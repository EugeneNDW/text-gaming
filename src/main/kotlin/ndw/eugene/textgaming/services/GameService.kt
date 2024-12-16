package ndw.eugene.textgaming.services

import jakarta.persistence.EntityNotFoundException
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
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class GameService(
    private val gameStateRepository: GameStateRepository,
    private val conversationRepository: ConversationRepository,
    private val optionRepository: OptionRepository,
    private val locationRepository: LocationRepository,
    private val characterRepository: CharacterRepository,
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

    fun startGame(userId: Long): GameMessage {
        val currentGame = getUsersCurrentGame(userId) ?: throw IllegalArgumentException()
        processConversation(currentGame)
        return getGameMessage(currentGame)
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
        locationEntity.startId = -1
        val savedLocation = locationRepository.save(locationEntity)

        val conversationRequest = location.firstConversationPart
        val translationEntity = TextTranslationEntity().apply {
            id = TextTranslationKey(0, "en")
            translatedText = conversationRequest.conversationText
        }
        val text = TextEntity()
        text.addTranslation(translationEntity)

        val conversation = ConversationEntity()
        conversation.character = characterRepository.findByName(conversationRequest.person) ?: throw IllegalArgumentException("Character not found")
        conversation.illustration = conversationRequest.illustration
        conversation.text = text
        conversation.processorId = conversationRequest.processorId
        conversation.locationId = savedLocation.id!!
        val savedConversation = conversationRepository.save(conversation)

        locationEntity.startId = savedConversation.id!!
        return locationRepository.save(locationEntity)
    }

    fun createConversation(
        locationId: Long,
        createConversation: ConversationRequest
    ): ConversationEntity {
        val location = locationRepository.findById(locationId).orElseThrow {
            IllegalArgumentException("Location not found")
        }
        val character = characterRepository.findByName(createConversation.person) ?: throw IllegalArgumentException("Character not found")

        val translationEntity = TextTranslationEntity().apply {
            id = TextTranslationKey(0, "en")
            translatedText = createConversation.conversationText
        }
        val text = TextEntity()
        text.addTranslation(translationEntity)

        val newConversation = ConversationEntity()
        newConversation.locationId = location.id!!
        newConversation.text = text
        newConversation.character = character
        newConversation.illustration = createConversation.illustration
        newConversation.processorId = createConversation.processorId

        return conversationRepository.save(newConversation)
    }

    fun getLocale(userId: Long): String {
        val gameState = getUsersCurrentGame(userId)
        return gameState?.lang ?: "EN"
    }

    fun updateLocale(userId: Long, locale: String): GameState {
        val gameState = getUsersCurrentGame(userId) ?: throw IllegalArgumentException()
        gameState.lang = locale
        return gameStateRepository.save(gameState)
    }

    fun createGameForUser(userId: Long, startLocation: String): GameState {
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
            conversation.character!!.name,
            SystemMessagesService.getLocalizedText(conversation.text, "EN"),
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
                SystemMessagesService.getLocalizedText(it.text, "EN"),
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

    fun createOption(locationIdFromRequest: Long, conversationId: Long, request: CreateLinkRequest) {
        val optionRequest = request.optionRequest
        val toConversationId = if (request.toConversationId != null) {
            request.toConversationId
        } else if (request.conversationRequest != null) {
            val conversationRequest = request.conversationRequest

            val translationEntity = TextTranslationEntity().apply {
                id = TextTranslationKey(0, "en")
                translatedText = conversationRequest.conversationText
            }
            val textEntity = TextEntity()
            textEntity.addTranslation(translationEntity)

            val conversationToSave = ConversationEntity().apply {
                character = characterRepository.findByName(conversationRequest.person) ?: throw IllegalArgumentException("Character not found")
                text = textEntity
                processorId = conversationRequest.processorId
                illustration = conversationRequest.illustration
                this.locationId = locationIdFromRequest
            }
            val savedConversation = conversationRepository.save(conversationToSave)
            savedConversation.id!!
        } else {
            throw IllegalArgumentException("Either toConversationId or conversationRequest must be provided.")
        }

        val translationEntity = TextTranslationEntity().apply {
            id = TextTranslationKey(0, "en")
            translatedText = optionRequest.optionText
        }
        val textEntity = TextEntity()
        textEntity.addTranslation(translationEntity)

        val optionToSave = OptionEntity().apply {
            locationId = locationIdFromRequest
            fromId = conversationId
            toId = toConversationId
            text = textEntity
            optionCondition = optionRequest.optionConditionId
        }
        optionRepository.save(optionToSave)
    }

    fun getOptionsByConversationId(
        locationId: Long,
        conversationId: Long
    ): List<OptionResponse> {
        // Validate that the conversation exists and belongs to the location
        val conversation = conversationRepository.findById(conversationId).orElseThrow {
            EntityNotFoundException("Conversation with ID $conversationId not found")
        }

        if (conversation.locationId != locationId) {
            throw IllegalArgumentException("Conversation ID $conversationId does not belong to Location ID $locationId")
        }

        val options = optionRepository.findByFromId(conversationId)

        // Fetch all toConversations in one query to avoid N+1 problem
        val toIds = options.map { it.toId }.distinct()
        val toConversations = conversationRepository.findAllById(toIds)
        val toConversationsMap = toConversations.associateBy { it.id }

        return options.map { option ->
            val toConversationEntity = toConversationsMap[option.toId]

            val toConversationResponse = toConversationEntity?.let { toConv ->
                ConversationResponse(
                    id = toConv.id!!,
                    person = toConv.character!!.name,
                    conversationText = SystemMessagesService.getLocalizedText(toConv.text, "en"),
                    processorId = toConv.processorId,
                    illustration = toConv.illustration,
                    locationId = toConv.locationId
                )
            }

            OptionResponse(
                id = option.id!!,
                fromId = option.fromId,
                toId = option.toId,
                optionText = SystemMessagesService.getLocalizedText(option.text, "en  "),
                optionConditionId = option.optionCondition,
                locationId = conversation.locationId,
                toConversation = toConversationResponse
            )
        }
    }

    fun createCharacter(request: CharacterRequest): CharacterResponse {
        val characterEntity = CharacterEntity().apply {
            name = request.name
        }
        val savedCharacter = characterRepository.save(characterEntity)
        return CharacterResponse(
            id = savedCharacter.id!!,
            name = savedCharacter.name
        )
    }

    fun getAllCharacters(): List<CharacterResponse> {
        val characters: List<CharacterEntity> = characterRepository.findAll()
        return characters.map { character ->
            CharacterResponse(
                id = character.id!!,
                name = character.name
            )
        }
    }
}