package ndw.eugene.textgaming.structure.services

import ndw.eugene.textgaming.structure.data.ConversationPart
import ndw.eugene.textgaming.structure.data.Option
import ndw.eugene.textgaming.structure.data.UserOption
import ndw.eugene.textgaming.structure.data.GameState
import java.util.UUID

class ConversationService(
    private val historyService: HistoryService,
    private val locationService: LocationService
) {
    fun processConversation(gameState: GameState) {
        getCurrentConversation(gameState).executable.invoke(gameState)
    }

    fun progressConversation(gameState: GameState, optionId: UUID) {
        val option = getOptions(gameState).find { it.uuid == optionId }
            ?: throw IllegalArgumentException("no option with id: $optionId")
        if (!option.condition.invoke(gameState)) throw IllegalArgumentException("not available option")

        historyService.addSelectedOption(gameState.gameId, option.uuid)
        gameState.currentConversationId = option.toId
    }

    fun getCurrentConversation(gameState: GameState): ConversationPart {
        val location = locationService.getLocationData(gameState.location)

        return location.convById[gameState.currentConversationId] ?: throw IllegalArgumentException()
    }

    fun getAvailableOptions(gameState: GameState): List<UserOption> {
        val options = getOptions(gameState).map {
            val selected = historyService.isOptionSelected(gameState.gameId, it.uuid)
            val available = it.condition.invoke(gameState)

            UserOption(it, available, selected)
        }

        return options
    }

    private fun getOptions(gameState: GameState): List<Option> {
        val locationData = locationService.getLocationData(gameState.location)

        return locationData.convToOption[gameState.currentConversationId] ?: listOf()
    }
}