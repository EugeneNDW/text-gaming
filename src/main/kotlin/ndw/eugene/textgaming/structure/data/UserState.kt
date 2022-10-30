package ndw.eugene.textgaming.structure.data

import kotlinx.serialization.Serializable
import ndw.eugene.textgaming.content.Location

@Serializable
data class UserState(
    val userId: Long,
    var currentConversationId: Long,
    var location: Location,
)