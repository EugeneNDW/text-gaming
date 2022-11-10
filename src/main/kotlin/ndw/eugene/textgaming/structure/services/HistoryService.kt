package ndw.eugene.textgaming.structure.services

import java.util.*

class HistoryService {
    private val historyTracker = mutableMapOf<Long, MutableSet<UUID>>() //todo положить в базу

    fun addSelectedOption(userId: Long, optionId: UUID) {
        val usersOptions = historyTracker.getOrPut(userId) { mutableSetOf() }

        usersOptions.add(optionId)
    }

    fun isOptionSelected(userId: Long, optionId: UUID): Boolean {
        val usersOptions = historyTracker[userId] ?: return false

        return usersOptions.contains(optionId)
    }
}