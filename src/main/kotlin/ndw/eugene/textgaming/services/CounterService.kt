package ndw.eugene.textgaming.services

import ndw.eugene.textgaming.data.entity.Counter
import ndw.eugene.textgaming.data.entity.GameCounter
import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.data.repository.CounterRepository
import org.springframework.stereotype.Service

@Service
class CounterService(
    private val counterRepository: CounterRepository
) {

    fun createCounterIfNotExists(counterName: String) {
        val counterByName = counterRepository.findByName(counterName)
        if (counterByName != null) {
            return
        }

        val counter = Counter()
        counter.name = counterName
        counterRepository.save(counter)
    }

    fun increaseCounter(gameState: GameState, name: String) {
        val counter = getOrCreateCounter(gameState, name)
        counter.counterValue++
    }

    fun decreaseCounter(gameState: GameState, name: String) {
        val counter = getOrCreateCounter(gameState, name)

        counter.counterValue--
    }

    private fun getOrCreateCounter(gameState: GameState, name: String): GameCounter {
        val counter = gameState.gameCounters.find { it.counter.name == name }
        if (counter != null) {
            return counter
        }

        val newCounter = GameCounter()
        newCounter.counter =
            counterRepository.findByName(name) ?: throw IllegalArgumentException("can't find counter with name $name")
        gameState.addCounter(newCounter)
        return newCounter
    }
}