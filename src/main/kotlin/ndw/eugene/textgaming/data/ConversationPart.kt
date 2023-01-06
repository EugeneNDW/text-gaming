package ndw.eugene.textgaming.data

import kotlinx.serialization.Serializable
import ndw.eugene.textgaming.content.ConversationProcessor
import ndw.eugene.textgaming.content.GameCharacter

@Serializable
data class ConversationPart(
    val id: Long,
    val character: GameCharacter,
    val text: String,
    val processorId: String? = null,
    val illustration: String? = null,
    var executable: ConversationProcessor = { } // получает стейт чтобы изменить его
)
