package ndw.eugene.textgaming.data

import ndw.eugene.textgaming.content.ConversationProcessor
import ndw.eugene.textgaming.content.GameCharacter

data class ConversationPart(
    val id: Long,
    val character: GameCharacter,
    val text: String,
    val illustration: String? = null,
    var executable: ConversationProcessor = { } // получает стейт чтобы изменить его
)
