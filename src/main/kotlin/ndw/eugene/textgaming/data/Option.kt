package ndw.eugene.textgaming.data

import ndw.eugene.textgaming.content.OptionCondition
import java.util.UUID

data class Option(
    val uuid: UUID,
    val fromId: Long,
    val toId: Long,
    val optionText: String,
    val condition: OptionCondition = { true }
)

data class UserOption(
    val option: Option,
    val available: Boolean,
    val selected: Boolean,
)