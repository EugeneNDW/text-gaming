package ndw.eugene.textgaming.services

import mu.KotlinLogging
import ndw.eugene.textgaming.controllers.*
import ndw.eugene.textgaming.data.ConversationPart
import ndw.eugene.textgaming.data.GameMessage
import ndw.eugene.textgaming.data.Option
import ndw.eugene.textgaming.data.UserOption
import ndw.eugene.textgaming.data.entity.*
import ndw.eugene.textgaming.data.repository.*
import ndw.eugene.textgaming.loaders.IllustrationsLoader
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class GameService(
    private val gameStateRepository: GameStateRepository,
    private val conversationRepository: ConversationRepository,
    private val optionRepository: OptionRepository,
    private val locationRepository: LocationRepository,
    private val choiceRepository: ChoiceRepository,
    private val counterRepository: CounterRepository,
    private val processorService: ProcessorService,
    private val illustrationsLoader: IllustrationsLoader,
    private val conditionService: ConditionService
) {
    fun userHasGameActive(userId: Long): Boolean {
        val usersCurrentGame = getUsersCurrentGame(userId)
        return usersCurrentGame != null && !usersCurrentGame.isEnded
    }

    fun startNewGameForUser(userId: Long, location: String = "DOCKS"): GameMessage {
        val game = createGameForUser(userId, location)
        processConversation(game)
        val savedGame = gameStateRepository.save(game)

        logger.info { "game with id: ${savedGame.id} for user: $userId was started in location: $location" }
        return getGameMessage(savedGame)
    }

    fun chooseOption(userId: Long, optionId: String): GameMessage {
        logger.info { "user: $userId chose option: $optionId" }
        val game = getUsersCurrentGame(userId) ?: throw IllegalArgumentException()
        val optionUUID = UUID.fromString(optionId)

        progressConversation(game, optionUUID)
        processConversation(game)

        val savedGame = gameStateRepository.save(game)

        return getGameMessage(savedGame)
    }

    fun getUsersCurrentGame(userId: Long): GameState? {
        return gameStateRepository.findGameStateWithMaxIdByUserId(userId)
    }

    fun getUserCurrentPlace(userId: Long): GameMessage {
        val game = getUsersCurrentGame(userId) ?: throw IllegalArgumentException()
        return getGameMessage(game)
    }

    fun createLocation(
        location: LocationRequest
    ): LocationEntity {
        val locationEntity = LocationEntity()
        locationEntity.name = location.name
        locationEntity.startId = location.startId
        return locationRepository.save(locationEntity)
    }

    fun createConversation(
        locationId: Long,
        createConversation: ConversationRequest
    ): ConversationEntity {
        val location = locationRepository.findById(locationId).orElseThrow {
            IllegalArgumentException("Location not found")
        }
        val newConversation = ConversationEntity()
        newConversation.locationId = location.id!!
        newConversation.conversationText = createConversation.conversationText
        newConversation.person = createConversation.person
        newConversation.illustration = createConversation.illustration
        newConversation.processorId = createConversation.processorId

        return conversationRepository.save(newConversation)
    }

    fun createOptions(
        locationId: Long,
        options: List<OptionRequest>
    ): MutableList<OptionEntity> {
        val location = locationRepository.findById(locationId).orElseThrow {
            IllegalArgumentException("Location not found")
        }
        val entities = options.map {
            val option = OptionEntity()
            option.fromId = it.fromId
            option.toId = it.toId
            option.optionText = it.optionText
            option.optionCondition = it.optionConditionId
            option.locationId = location.id!!
            option
        }.toList()

        return optionRepository.saveAll(entities)
    }

    private fun createGameForUser(userId: Long, startLocation: String): GameState {
        val locationEntity = locationRepository.findByName(startLocation)
        val conversationStartId = locationEntity?.startId ?: throw IllegalArgumentException()
        val game = GameState()
        game.userId = userId
        game.location = startLocation
        game.currentConversationId = conversationStartId

        return gameStateRepository.save(game)
    }

    private fun getGameMessage(gameState: GameState): GameMessage {
        val conversationPart = getCurrentConversation(gameState)
        val options = getAvailableOptions(gameState)

        return GameMessage(conversationPart, options)
    }

    private fun processConversation(gameState: GameState) {
        processorService.executeProcessor(gameState, getCurrentConversation(gameState).processor)
    }

    private fun progressConversation(gameState: GameState, optionId: UUID) {
        val option = getOptions(gameState.currentConversationId, gameState.location)
            .find { it.uuid == optionId } ?: throw IllegalArgumentException("no option with id: $optionId")
        if (!conditionService.evaluateCondition(
                option.condition,
                gameState
            )
        ) throw IllegalArgumentException("not available option")

        addSelectedOption(gameState, optionId)
        gameState.currentConversationId = option.toId
    }

    private fun getCurrentConversation(gameState: GameState): ConversationPart {
        val locationName = gameState.location
        val conversation =
            conversationRepository.getByLocationAndConversationId(locationName, gameState.currentConversationId)
        return ConversationPart(
            conversation.id!!,
            conversation.person,
            conversation.conversationText,
            illustrationsLoader.getIllustration(conversation.illustration),
            conversation.processorId
        )
    }

    private fun getAvailableOptions(gameState: GameState): List<UserOption> {
        val options = getOptions(gameState.currentConversationId, gameState.location).map {
            val selected = isOptionSelected(gameState, it.uuid)
            val available = conditionService.evaluateCondition(it.condition, gameState)

            UserOption(it, available, selected)
        }

        return options
    }

    private fun addSelectedOption(gameState: GameState, optionId: UUID) {
        val selectedOption = GameHistory()
        selectedOption.optionId = optionId
        gameState.addHistory(selectedOption)
    }

    private fun isOptionSelected(gameState: GameState, optionId: UUID): Boolean {
        return gameState.gameHistory.find { it.optionId == optionId } != null
    }

    private fun getOptions(currentConversationId: Long, location: String): List<Option> {
        return optionRepository.findAllByFromIdAndLocation(currentConversationId, location).map {
            val option = Option(
                it.id!!,
                it.fromId,
                it.toId,
                it.optionText,
                it.optionCondition
            )
            option
        }
    }

    fun getOptionsByLocation(locationId: Long): List<OptionEntity> {
        return optionRepository.findByLocationId(locationId)
    }

    fun getConversationsByLocation(locationId: Long): List<ConversationEntity> {
        return conversationRepository.findByLocationId(locationId)
    }

    fun getAllLocations(): List<LocationEntity> {
        return locationRepository.findAll()
    }

    fun getAllCounters(): List<Counter> {
        return counterRepository.findAll()
    }

    fun getAllChoices(): List<Choice> {
        return choiceRepository.findAll()
    }

    fun createCounter(counterRequest: CounterRequest): Counter {
        val counterEntity = Counter()
        counterEntity.name = counterRequest.name

        return counterRepository.save(counterEntity)
    }

    fun createChoices(choiceRequest: ChoiceRequest): Choice {
        val choiceEntity = Choice()
        choiceEntity.name = choiceRequest.name
        return choiceRepository.save(choiceEntity)
    }
}