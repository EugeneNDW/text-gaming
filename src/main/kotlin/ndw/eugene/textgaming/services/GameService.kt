package ndw.eugene.textgaming.services

import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import ndw.eugene.textgaming.content.ConversationProcessors
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.controllers.*
import ndw.eugene.textgaming.data.ConversationPart
import ndw.eugene.textgaming.data.GameMessage
import ndw.eugene.textgaming.data.Option
import ndw.eugene.textgaming.data.UserOption
import ndw.eugene.textgaming.data.entity.*
import ndw.eugene.textgaming.data.repository.ConversationRepository
import ndw.eugene.textgaming.data.repository.GameStateRepository
import ndw.eugene.textgaming.data.repository.LocationRepository
import ndw.eugene.textgaming.data.repository.OptionRepository
import ndw.eugene.textgaming.loaders.ConversationLoader
import ndw.eugene.textgaming.loaders.IllustrationsLoader
import org.springframework.stereotype.Service
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class GameService(
    private val conversationLoader: ConversationLoader,
    private val gameStateRepository: GameStateRepository,
    private val conversationRepository: ConversationRepository,
    private val optionRepository: OptionRepository,
    private val locationRepository: LocationRepository,
    private val conversationProcessors: ConversationProcessors,
    private val illustrationsLoader: IllustrationsLoader,
    private val conditionService: ConditionService
) {
    @PostConstruct
    fun initGameService() {
        conversationLoader.loadLocations()
        logger.info { "game service was initialized" }
    }

    fun userHasGameActive(userId: Long): Boolean {
        val usersCurrentGame = getUsersCurrentGame(userId)
        return usersCurrentGame != null && !usersCurrentGame.isEnded
    }

    fun startNewGameForUser(userId: Long, location: Location = Location.DOCKS): GameMessage {
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
        location: CreateLocationRequest
    ): CreateLocationResponse {
        val locationEntity = LocationEntity()
        locationEntity.name = location.name
        locationEntity.startId = location.startId
        val savedLocation = locationRepository.save(locationEntity)

        return CreateLocationResponse(savedLocation.id!!, savedLocation.name, savedLocation.startId)
    }

    fun createConversation(
        createConversation: CreateConversationRequest
    ): CreateConversationResponse {
        val newConversation = ConversationEntity()
        newConversation.locationId = createConversation.locationId
        newConversation.conversationText = createConversation.conversationText
        newConversation.person = createConversation.person
        newConversation.illustration = createConversation.illustration
        newConversation.processorId = createConversation.processorId

        val savedConversation = conversationRepository.save(newConversation)

        return CreateConversationResponse(
            savedConversation.id!!,
            savedConversation.person,
            savedConversation.conversationText,
            savedConversation.processorId,
            savedConversation.illustration,
            savedConversation.locationId
        )
    }

    fun createOptions(
        options: List<OptionRequest>
    ): List<OptionResponse> {
        val entities = options.map {
            val option = OptionEntity()
            option.fromId = it.fromId
            option.toId = it.toId
            option.optionText = it.optionText
            option.optionCondition = it.optionConditionId
            option.locationId = it.locationId
            option
        }.toList()

        return optionRepository.saveAll(entities).map {
            val option = OptionResponse(
                it.id!!,
                it.fromId,
                it.toId,
                it.optionText,
                it.optionCondition,
                it.locationId,
            )
            option
        }
    }

    private fun createGameForUser(userId: Long, startLocation: Location): GameState {
        val locationEntity = locationRepository.findByName(startLocation.name)
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
        conversationProcessors.executeProcessor(gameState, getCurrentConversation(gameState).processor)
    }

    private fun progressConversation(gameState: GameState, optionId: UUID) {
        val option = getOptions(gameState.currentConversationId, gameState.location.name)
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
        val locationName = gameState.location.name
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
        val options = getOptions(gameState.currentConversationId, gameState.location.name).map {
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
}