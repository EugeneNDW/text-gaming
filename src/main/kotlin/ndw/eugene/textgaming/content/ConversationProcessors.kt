package ndw.eugene.textgaming.content

import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.services.ChoiceService
import ndw.eugene.textgaming.services.CounterService
import ndw.eugene.textgaming.services.LocationService
import org.springframework.stereotype.Component

@Component
class ConversationProcessors(
    private val choiceService: ChoiceService,
    private val locationService: LocationService,
    private val counterService: CounterService,
) {
    fun executeProcessor(gameState: GameState, processorString: String) {
        if (processorString.isBlank()) {
            return
        }
        val (action, value) = processorString.split(':').let { it[0] to it[1] }
        when (action) {
            "CHANGE" -> changeLocationTo(gameState, value)
            "MEMORIZE" -> addChoice(gameState, value)
            "INCREASE" -> increaseCounter(gameState, value)
            "DECREASE" -> decreaseCounter(gameState, value)
            "END" -> endGame(gameState)
            else -> println("Unknown action: $action and value: $value")
        }
    }

    private fun changeLocationTo(gameState: GameState, location: String) {
        locationService.changeLocationTo(gameState, location)
    }

    private fun addChoice(gameState: GameState, choice: String) {
        choiceService.addChoice(gameState, choice)
    }

    private fun increaseCounter(gameState: GameState, counter: String) {
        counterService.increaseCounter(gameState, counter)
    }

    private fun decreaseCounter(gameState: GameState, counter: String) {
        counterService.decreaseCounter(gameState, counter)
    }

    private fun endGame(gameState: GameState) {
        gameState.isEnded = true
        locationService.changeLocationTo(gameState, "END")
    }
}