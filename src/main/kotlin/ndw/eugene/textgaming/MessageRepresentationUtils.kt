package ndw.eugene.textgaming

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import ndw.eugene.textgaming.content.Choice
import ndw.eugene.textgaming.content.GameCharacter
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.data.ConversationPart
import ndw.eugene.textgaming.data.UserOption
import ndw.eugene.textgaming.data.entity.GameState

private const val DEFAULT_OPTION_TEXT = "..."
private const val DEFAULT_OPTION_BUTTON_TEXT = "continue..."
private const val NEW_GAME_BUTTON_TEXT = "start new game"
private const val TEXT_OPTIONS_DELIMITER = "\n\n"
private const val CHARACTER_TEXT_DELIMITER = "\n"
const val BUTTON_ID_DELIMITER = ":"

enum class ButtonId {
    CHOOSE, UNCHOOSE, OPTION, START, LOCATION
}

fun formatResponse(conversationPart: ConversationPart, options: List<UserOption>): String {
    var result = ""

    if (conversationPart.character != GameCharacter.ADMIN) {
        result += makeTextBold("${conversationPart.character}:")
        result += CHARACTER_TEXT_DELIMITER
    }

    result += conversationPart.text
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
            callbackData = ButtonId.START.name
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
                callbackData = "${ButtonId.OPTION.name}$BUTTON_ID_DELIMITER${options[0].option.uuid}"
            )
        )
    } else {
        options.forEachIndexed { index, userOption ->
            if (userOption.available) {
                buttons.add(
                    InlineKeyboardButton.CallbackData(
                        text = "${index + 1}",
                        callbackData = "${ButtonId.OPTION.name}$BUTTON_ID_DELIMITER${userOption.option.uuid}"
                    )
                )
            }
        }
    }

    return InlineKeyboardMarkup.create(buttons)
}

fun getLocationsButtons(gameState: GameState): InlineKeyboardMarkup {
    val buttons: MutableList<List<InlineKeyboardButton>> = mutableListOf()

    Location.values().forEach {
        var text = it.name
        if (it == gameState.location) {
            text += "✅"
        }
        buttons.add(
            listOf(
                InlineKeyboardButton.CallbackData(
                    text = text,
                    callbackData = "${ButtonId.LOCATION.name}$BUTTON_ID_DELIMITER${it.name}"
                )
            )
        )
    }

    return InlineKeyboardMarkup.create(buttons)
}

fun getChoicesButtons(gameState: GameState): InlineKeyboardMarkup {
    val buttons: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    val chosen = gameState.gameChoices.map { it.choice }
    val notChosen = Choice.values().toSet().minus(chosen.toSet())

    chosen.forEach {
        buttons.add(
            listOf(
                createUnchooseButtonForChoice(it)
            )
        )
    }

    notChosen.forEach {
        buttons.add(
            listOf(
                createChooseButtonForChoice(it)
            )
        )
    }

    return InlineKeyboardMarkup.create(buttons)
}

private fun createUnchooseButtonForChoice(choice: Choice): InlineKeyboardButton.CallbackData {
    val text = choice.name + "✅"

    return InlineKeyboardButton.CallbackData(
        text = text,
        callbackData = "${ButtonId.UNCHOOSE.name}$BUTTON_ID_DELIMITER${choice.name}"
    )
}

private fun createChooseButtonForChoice(choice: Choice): InlineKeyboardButton.CallbackData {
    val text = choice.name + "❌"

    return InlineKeyboardButton.CallbackData(
        text = text,
        callbackData = "${ButtonId.CHOOSE.name}$BUTTON_ID_DELIMITER${choice.name}"
    )
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