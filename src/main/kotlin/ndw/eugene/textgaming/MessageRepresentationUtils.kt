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

fun formatResponse(conversationPart: ConversationPart, options: List<UserOption>): String {
    var result = ""
    result += makeTextBold(
        prepareForMarkdown("${conversationPart.character}:")
    ) +
        "\n" + prepareForMarkdown(conversationPart.text)
    result += "\n\n"

    val hasOnlyDefaultOption = options.size == 1 && options[0].option.optionText == DEFAULT_OPTION_TEXT
    if (!hasOnlyDefaultOption) {
        options.forEachIndexed { index, userOption ->
            val text = "${index + 1}. ${userOption.option.optionText}"
            val preparedText = prepareForMarkdown(text)
            result += if (!userOption.selected) {
                makeTextBold(preparedText)
            } else {
                makeStrikeThrough(preparedText)
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
        val newText = editText(messageText)

        bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = newText,
            parseMode = ParseMode.MARKDOWN_V2
        )
    } else if (messageCaption != null) {
        val newText = editText(messageCaption)

        bot.editMessageCaption(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            caption = newText,
            parseMode = ParseMode.MARKDOWN_V2
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
            text = "start new game",
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

private fun editText(text: String): String {
    val messageTokens = text.split("\n\n")
    val messageWithoutOptions = messageTokens[0]
    val messageWithoutOptionsTokens = messageWithoutOptions.split("\n")

    return makeTextBold(prepareForMarkdown(messageWithoutOptionsTokens[0])) + " \n" + prepareForMarkdown(
        messageWithoutOptionsTokens[1]
    )
}


private fun prepareForMarkdown(text: String): String {
    val result = StringBuilder()

    for (c in text) {
        when (c) {
            '*' -> result.append("\\*")
            '_' -> result.append("\\_")
            '[' -> result.append("\\[")
            ']' -> result.append("\\]")
            '(' -> result.append("\\(")
            ')' -> result.append("\\)")
            '#' -> result.append("\\#")
            '~' -> result.append("\\~")
            '`' -> result.append("\\`")
            '>' -> result.append("\\>")
            '+' -> result.append("\\+")
            '-' -> result.append("\\-")
            '=' -> result.append("\\=")
            '|' -> result.append("\\|")
            '{' -> result.append("\\{")
            '}' -> result.append("\\}")
            '.' -> result.append("\\.")
            '!' -> result.append("\\!")
            else -> result.append(c)
        }
    }

    return result.toString()
}


private fun makeTextBold(text: String): String {
    return "*$text*"
}

private fun makeStrikeThrough(text: String): String {
    return "~$text~"
}