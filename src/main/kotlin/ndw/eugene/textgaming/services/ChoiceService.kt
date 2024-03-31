package ndw.eugene.textgaming.services

import mu.KotlinLogging
import ndw.eugene.textgaming.content.Choice
import ndw.eugene.textgaming.data.entity.GameChoice
import ndw.eugene.textgaming.data.entity.GameState
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ChoiceService {
    fun addChoice(gameState: GameState, choice: String) {
        val gameChoice = GameChoice()
        gameChoice.choice = Choice.valueOf(choice) //todo remake to just string
        gameState.addChoice(gameChoice)
        logger.info { "user: ${gameState.userId} made choice: $choice in game: ${gameState.id}" }
    }

    fun checkChoiceHasBeenMade(gameState: GameState, choice: Choice): Boolean {
        val usersChoices = gameState.gameChoices

        val result = usersChoices.find { it.choice == choice } != null

        logger.info { "user: ${gameState.userId} in game: ${gameState.id} is checking choice: $choice, result is: $result" }
        return result
    }
}