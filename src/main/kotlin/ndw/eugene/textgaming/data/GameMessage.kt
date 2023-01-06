package ndw.eugene.textgaming.data

data class GameMessage(
    val currentConversation: ConversationPart,
    val options: List<UserOption>,
)