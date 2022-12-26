package ndw.eugene.textgaming.structure.services

import jakarta.annotation.PostConstruct
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.loaders.ConversationLoader
import ndw.eugene.textgaming.structure.data.GameMessage
import ndw.eugene.textgaming.structure.data.GameState
import org.springframework.stereotype.Service
import java.util.*

@Service
class GameService(
    private val locationService: LocationService,
    private val conversationService: ConversationService,
    private val conversationLoader: ConversationLoader,
    private val users: MutableMap<Long, MutableSet<GameState>> = mutableMapOf()
) {
    private var counter = 0L

    @PostConstruct
    fun initGameService() {
        val locations = conversationLoader.loadLocations()
        locationService.initLocationService(locations)
    }

    fun userHasGameStarted(userId: Long): Boolean {
        return getUsersCurrentGame(userId) != null
    }

    fun startNewGameForUser(userId: Long, location: Location = Location.DOCKS): GameMessage {
        val game = createGameForUser(userId, location)
        conversationService.processConversation(game)

        return getGameMessage(game)
    }

    fun chooseOption(userId: Long, optionId: String): GameMessage {
        val game = getUsersCurrentGame(userId) ?: throw IllegalArgumentException()
        val optionUUID = UUID.fromString(optionId)

        conversationService.progressConversation(game, optionUUID)
        conversationService.processConversation(game)

        return getGameMessage(game)
    }

    private fun createGameForUser(userId: Long, startLocation: Location): GameState {
        val conversationStartId = locationService.getLocationData(startLocation).startId
        val userGames = users.computeIfAbsent(userId) { mutableSetOf() }
        val game = GameState(getGameId(), conversationStartId, startLocation)
        userGames.add(game)

        return game
    }

    private fun getUsersCurrentGame(userId: Long): GameState? {
        return users[userId]?.maxBy { it.gameId } //game with max id is CURRENT GAME because game id generator is increment counter
    }

    private fun getGameId(): Long {
        return counter++
    }

    private fun getGameMessage(gameState: GameState): GameMessage {
        val conversationPart = conversationService.getCurrentConversation(gameState)
        val options = conversationService.getAvailableOptions(gameState)

        return GameMessage(conversationPart, options)
    }
}