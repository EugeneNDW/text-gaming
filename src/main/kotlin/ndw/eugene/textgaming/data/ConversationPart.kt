package ndw.eugene.textgaming.data

import ndw.eugene.textgaming.content.ConversationProcessor
import ndw.eugene.textgaming.content.GameCharacter

data class ConversationPart(
    val id: Long,
    val character: GameCharacter,
    val text: String,
    val illustration: ByteArray? = null,
    val executable: ConversationProcessor = { } // получает стейт чтобы изменить его
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConversationPart

        if (id != other.id) return false
        if (character != other.character) return false
        if (text != other.text) return false
        if (illustration != null) {
            if (other.illustration == null) return false
            if (!illustration.contentEquals(other.illustration)) return false
        } else if (other.illustration != null) return false
        if (executable != other.executable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + character.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + (illustration?.contentHashCode() ?: 0)
        result = 31 * result + executable.hashCode()
        return result
    }
}
