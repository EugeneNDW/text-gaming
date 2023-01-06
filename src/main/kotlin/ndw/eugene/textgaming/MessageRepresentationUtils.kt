package ndw.eugene.textgaming

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import ndw.eugene.textgaming.data.ConversationPart
import ndw.eugene.textgaming.data.UserOption

private const val DEFAULT_OPTION_TEXT = "..."
private const val DEFAULT_OPTION_BUTTON_TEXT = "continue..."
private const val NEW_GAME_BUTTON_TEXT = "start new game"
private const val TEXT_OPTIONS_DELIMITER = "\n\n"
private const val CHARACTER_TEXT_DELIMITER = "\n"

fun formatResponse(conversationPart: ConversationPart, options: List<UserOption>): String {
    var result = ""
    result += makeTextBold("${conversationPart.character}:") + CHARACTER_TEXT_DELIMITER + conversationPart.text
    result += TEXT_OPTIONS_DELIMITER

    val hasOnlyDefaultOption = options.size == 1 && options[0].option.optionText == DEFAULT_OPTION_TEXT
    if (!hasOnlyDefaultOption) {
        options.forEachIndexed { index, userOption ->
            val text = "${index + 1}. ${userOption.option.optionText}"
            result += if (!userOption.selected) {
                makeTextBold(text)
            } else {
                makeTextStrikethrough(text)
            }
            result += "\n"
        }
    }

    return result
}

fun editMessage(bot: Bot, message: Message) {
    val messageText = message.text
    val messageCaption = message.caption

    if (messageText != null) {
        val newText = removeOptionsFromMessage(messageText)

        bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = newText,
            parseMode = ParseMode.HTML
        )
    } else if (messageCaption != null) {
        val newText = removeOptionsFromMessage(messageCaption)

        bot.editMessageCaption(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            caption = newText,
            parseMode = ParseMode.HTML
        )
    } else {
        println(message)
        throw IllegalArgumentException("unknown type of message")
    }
}

fun createStartGameButton(): InlineKeyboardMarkup {
    val buttons: MutableList<InlineKeyboardButton> = mutableListOf()

    buttons.add(
        InlineKeyboardButton.CallbackData(
            text = NEW_GAME_BUTTON_TEXT,
            callbackData = "START_GAME"
        )
    )

    return InlineKeyboardMarkup.create(buttons)
}

fun optionsToButtons(options: List<UserOption>): InlineKeyboardMarkup {
    val buttons: MutableList<InlineKeyboardButton> = mutableListOf()

    val hasOnlyDefaultOption = options.size == 1 && options[0].option.optionText == DEFAULT_OPTION_TEXT
    if (hasOnlyDefaultOption) {
        buttons.add(
            InlineKeyboardButton.CallbackData(
                text = DEFAULT_OPTION_BUTTON_TEXT,
                callbackData = "${options[0].option.uuid}"
            )
        )
    } else {
        options.forEachIndexed { index, userOption ->
            if (userOption.available) {
                buttons.add(
                    InlineKeyboardButton.CallbackData(
                        text = "${index + 1}",
                        callbackData = "${userOption.option.uuid}"
                    )
                )
            }
        }
    }

    return InlineKeyboardMarkup.create(buttons)
}

private fun removeOptionsFromMessage(message: String): String {
    val messageTokens = message.split(TEXT_OPTIONS_DELIMITER)
    val characterAndText = messageTokens[0]
    val characterAndTextTokens = characterAndText.split(CHARACTER_TEXT_DELIMITER)
    val character = makeTextBold(characterAndTextTokens[0])

    return character + CHARACTER_TEXT_DELIMITER + characterAndTextTokens[1]
}

private fun makeTextBold(text: String): String {
    return "<b>$text</b>"
}

private fun makeTextStrikethrough(text: String): String {
    return "<s>$text</s>"
}