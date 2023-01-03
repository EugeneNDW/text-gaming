package ndw.eugene.textgaming.structure.services

import ndw.eugene.textgaming.content.Choice
import ndw.eugene.textgaming.structure.data.entity.GameChoice
import ndw.eugene.textgaming.structure.data.entity.GameState
import org.springframework.stereotype.Service

@Service
class ChoiceService {
    fun addChoice(gameState: GameState, choice: Choice) {
        val gameChoice = GameChoice()

        gameChoice.choice = choice
        gameState.addChoice(gameChoice)
    }

    fun checkChoiceHasBeenMade(gameState: GameState, choice: Choice): Boolean {
        val usersChoices = gameState.gameChoices

        return usersChoices.find { it.choice == choice } != null
    }
}