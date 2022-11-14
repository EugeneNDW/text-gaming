package ndw.eugene.textgaming.structure.data

import kotlinx.serialization.Serializable
import ndw.eugene.textgaming.content.GameCharacter

@Serializable
data class ConversationPart(
    val id: Long,
    val character: GameCharacter,
    val text: String,
    val processorId: String? = null,
    val illustration: String? = null,
    var executable: (UserState) -> Unit = { }
)
