package ndw.eugene.textgaming.structure.services

import ndw.eugene.textgaming.structure.data.ConversationPart
import ndw.eugene.textgaming.structure.data.Option
import ndw.eugene.textgaming.structure.data.UserOption
import ndw.eugene.textgaming.structure.data.UserState
import java.util.UUID

class ConversationService(
    private val historyService: HistoryService,
    private val locationService: LocationService
) {
    fun processConversation(userState: UserState) {
        val locationData = locationService.getLocationData(userState.location)
        locationData.convById[userState.currentConversationId]?.executable?.invoke(userState)
    }

    fun progressConversation(userState: UserState, optionId: UUID) {
        val option = getOptions(userState).find { it.uuid == optionId }
            ?: throw IllegalArgumentException("no option with id: $optionId")
        if (!option.condition.invoke(userState)) throw IllegalArgumentException("not available option")

        historyService.addSelectedOption(userState.userId, option.uuid)
        userState.currentConversationId = option.toId
    }

    fun getCurrentConversation(userState: UserState): ConversationPart {
        val location = locationService.getLocationData(userState.location)

        return location.convById[userState.currentConversationId] ?: throw IllegalArgumentException()
    }

    fun getAvailableOptions(userState: UserState): List<UserOption> {
        val options = getOptions(userState).map {
            val selected = historyService.isOptionSelected(userState.userId, it.uuid)
            val available = it.condition.invoke(userState)

            UserOption(it, available, selected)
        }

        return options
    }

    private fun getOptions(userState: UserState): List<Option> {
        val locationData = locationService.getLocationData(userState.location)

        return locationData.convToOption[userState.currentConversationId] ?: listOf()
    }
}