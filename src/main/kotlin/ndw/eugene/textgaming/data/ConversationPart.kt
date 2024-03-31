package ndw.eugene.textgaming.data

data class ConversationPart(
    val id: Long,
    val character: String,
    val text: String,
    val illustration: ByteArray? = null,
    val processor: String
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
        if (processor != other.processor) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + character.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + (illustration?.contentHashCode() ?: 0)
        return result
    }
}
