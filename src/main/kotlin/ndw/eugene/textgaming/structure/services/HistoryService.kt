package ndw.eugene.textgaming.structure.services

import org.springframework.stereotype.Service
import java.util.*

@Service
class HistoryService {
    private val historyTracker = mutableMapOf<Long, MutableSet<UUID>>() //todo положить в базу

    fun addSelectedOption(gameId: Long, optionId: UUID) {
        val usersOptions = historyTracker.getOrPut(gameId) { mutableSetOf() }

        usersOptions.add(optionId)
    }

    fun isOptionSelected(gameId: Long, optionId: UUID): Boolean {
        val usersOptions = historyTracker[gameId] ?: return false

        return usersOptions.contains(optionId)
    }
}