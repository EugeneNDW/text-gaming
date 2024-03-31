package ndw.eugene.textgaming.services

import ndw.eugene.textgaming.data.entity.CounterType
import ndw.eugene.textgaming.data.entity.GameCounter
import ndw.eugene.textgaming.data.entity.GameState
import org.springframework.stereotype.Service

@Service
class CounterService {

    fun increaseCounter(gameState: GameState, type: String) {
        val counterType = CounterType.valueOf(type)
        val counter = getOrCreateCounter(gameState, counterType)

        counter.counterValue++
    }

    fun decreaseCounter(gameState: GameState, type: String) {
        val counterType = CounterType.valueOf(type)
        val counter = getOrCreateCounter(gameState, counterType)

        counter.counterValue--
    }

    fun getCounterValue(gameState: GameState, type: CounterType): Int {
        val counter = gameState.gameCounters.find { it.counterType == type }

        return counter?.counterValue ?: 0
    }

    private fun getOrCreateCounter(gameState: GameState, type: CounterType): GameCounter {
        var counter = gameState.gameCounters.find { it.counterType == type }

        if (counter == null) {
            counter = GameCounter()
            counter.counterType = type
            gameState.addCounter(counter)
        }

        return counter
    }
}