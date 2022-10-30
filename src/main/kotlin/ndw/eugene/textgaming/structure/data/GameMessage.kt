package ndw.eugene.textgaming.structure.data

data class GameMessage(
    val currentConversation: ConversationPart,
    val options: List<UserOption>,
)