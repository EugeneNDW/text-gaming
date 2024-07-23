package ndw.eugene.textgaming.services

import ndw.eugene.textgaming.data.repository.GameStateRepository
import org.springframework.stereotype.Service

@Service
class ManagerService(
    private val gameService: GameService,
    private val choiceService: ChoiceService,
    private val gameStateRepository: GameStateRepository,
    private val locationService: LocationService,
) {

    fun changeLocation(userId: Long, locationName: String) {
        val game = gameService.getUsersCurrentGame(userId) ?: throw IllegalArgumentException()
        val location = locationService.getByName(locationName)

        game.location = location.name
        game.currentConversationId = location.startId
        gameStateRepository.save(game)
    }

    fun addChoice(userId: Long, choice: String) {
        val game = gameService.getUsersCurrentGame(userId) ?: throw IllegalArgumentException()
        choiceService.addChoice(game, choice)
        gameStateRepository.save(game)
    }

    fun removeChoice(userId: Long, choice: String) {
        val game = gameService.getUsersCurrentGame(userId) ?: throw IllegalArgumentException()
        val choiceToRemove = game.gameChoices.find { it.choice.name == choice } ?: return
        game.removeChoice(choiceToRemove)
        gameStateRepository.save(game)
    }
}