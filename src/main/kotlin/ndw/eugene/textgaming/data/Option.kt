package ndw.eugene.textgaming.data

import java.util.*

data class Option(
    val uuid: UUID,
    val fromId: Long,
    val toId: Long,
    val optionText: String,
    val condition: String
)

data class UserOption(
    val option: Option,
    val available: Boolean,
    val selected: Boolean,
)