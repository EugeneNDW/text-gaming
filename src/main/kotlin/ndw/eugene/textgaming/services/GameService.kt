package ndw.eugene.textgaming.services

import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.data.ConversationPart
import ndw.eugene.textgaming.data.GameMessage
import ndw.eugene.textgaming.data.Option
import ndw.eugene.textgaming.data.UserOption
import ndw.eugene.textgaming.data.entity.GameHistory
import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.data.repository.GameStateRepository
import ndw.eugene.textgaming.loaders.ConversationLoader
import org.springframework.stereotype.Service
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class GameService(
    private val locationService: LocationService,
    private val conversationLoader: ConversationLoader,
    private val gameStateRepository: GameStateRepository,
) {
    @PostConstruct
    fun initGameService() {
        val locations = conversationLoader.loadLocations()
        locationService.initLocationService(locations)

        logger.info { "game service was initialized" }
    }

    fun userHasGameActive(userId: Long): Boolean {
        val usersCurrentGame = getUsersCurrentGame(userId)
        return usersCurrentGame != null && !usersCurrentGame.isEnded //todo отдельный запрос к репо, который проверяет есть ли у нас начатая игра
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

    private fun createGameForUser(userId: Long, startLocation: Location): GameState {
        val conversationStartId = locationService.getLocationData(startLocation).startId
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
        getCurrentConversation(gameState).executable.invoke(gameState)
    }

    private fun progressConversation(gameState: GameState, optionId: UUID) {
        val option = getOptions(gameState).find { it.uuid == optionId }
            ?: throw IllegalArgumentException("no option with id: $optionId")
        if (!option.condition.invoke(gameState)) throw IllegalArgumentException("not available option")

        addSelectedOption(gameState, optionId)
        gameState.currentConversationId = option.toId
    }

    private fun getCurrentConversation(gameState: GameState): ConversationPart {
        val location = locationService.getLocationData(gameState.location)

        return location.convById[gameState.currentConversationId] ?: throw IllegalArgumentException()
    }

    private fun getAvailableOptions(gameState: GameState): List<UserOption> {
        val options = getOptions(gameState).map {
            val selected = isOptionSelected(gameState, it.uuid)
            val available = it.condition.invoke(gameState)

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

    private fun getOptions(gameState: GameState): List<Option> {
        val locationData = locationService.getLocationData(gameState.location)

        return locationData.convToOption[gameState.currentConversationId] ?: listOf()
    }
}