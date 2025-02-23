package ndw.eugene.textgaming

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import ndw.eugene.textgaming.data.ConversationPart
import ndw.eugene.textgaming.data.UserOption
import ndw.eugene.textgaming.data.entity.Choice
import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.data.entity.LocationEntity
import ndw.eugene.textgaming.services.Locale
import ndw.eugene.textgaming.services.SystemMessageType
import ndw.eugene.textgaming.services.SystemMessagesService

private const val TEXT_OPTIONS_DELIMITER = "\n\n"
private const val CHARACTER_TEXT_DELIMITER = "\n"
const val DEFAULT_OPTION_TEXT = "..."
const val BUTTON_ID_DELIMITER = ":"

enum class ButtonId {
    CHOOSE, UNCHOOSE, OPTION, START, START_NEW, LOCATION, LOCALE
}

fun formatResponse(conversationPart: ConversationPart, options: List<UserOption>): String {
    var result = ""

    if (conversationPart.character != "ADMIN") {
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

fun createLanguageButtons(): InlineKeyboardMarkup {
    val buttons: MutableList<InlineKeyboardButton> = mutableListOf()
    Locale.entries.forEach {
        buttons.add(
            InlineKeyboardButton.CallbackData(
                text = it.name,
                callbackData = ButtonId.LOCALE.name + BUTTON_ID_DELIMITER + it.name
            )
        )
    }
    return InlineKeyboardMarkup.create(buttons)
}

fun createStartNewGameButton(locale: String): InlineKeyboardMarkup {
    val buttons: MutableList<InlineKeyboardButton> = mutableListOf()

    buttons.add(
        InlineKeyboardButton.CallbackData(
            text = SystemMessagesService.getMessage(locale, SystemMessageType.NEW_GAME_BUTTON_TEXT),
            callbackData = ButtonId.START_NEW.name
        )
    )

    return InlineKeyboardMarkup.create(buttons)
}

fun createStartGameButton(locale: String): InlineKeyboardMarkup {
    val buttons: MutableList<InlineKeyboardButton> = mutableListOf()

    buttons.add(
        InlineKeyboardButton.CallbackData(
            text = SystemMessagesService.getMessage(locale, SystemMessageType.NEW_GAME_BUTTON_TEXT),
            callbackData = ButtonId.START.name
        )
    )

    return InlineKeyboardMarkup.create(buttons)
}

fun optionsToButtons(locale: String, options: List<UserOption>): InlineKeyboardMarkup {
    val buttons: MutableList<InlineKeyboardButton> = mutableListOf()

    val hasOnlyDefaultOption = options.size == 1 && options[0].option.optionText == DEFAULT_OPTION_TEXT
    if (hasOnlyDefaultOption) {
        buttons.add(
            InlineKeyboardButton.CallbackData(
                text = SystemMessagesService.getMessage(locale, SystemMessageType.DEFAULT_OPTION_BUTTON_TEXT),
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

fun getLocationsButtons(gameState: GameState, locations: List<LocationEntity>): InlineKeyboardMarkup {
    val buttons: MutableList<List<InlineKeyboardButton>> = mutableListOf()

    locations.forEach {
        var text = it.name
        if (it.name == gameState.location) {
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

fun getChoicesButtons(gameState: GameState, allChoices: List<Choice>): InlineKeyboardMarkup {
    val buttons: MutableList<List<InlineKeyboardButton>> = mutableListOf()
    val chosen = gameState.gameChoices.map { it.choice }
    val notChosen = allChoices.minus(chosen.toSet())

    chosen.forEach {
        buttons.add(
            listOf(
                createUnchooseButtonForChoice(it.name)
            )
        )
    }

    notChosen.forEach {
        buttons.add(
            listOf(
                createChooseButtonForChoice(it.name)
            )
        )
    }

    return InlineKeyboardMarkup.create(buttons)
}

private fun createUnchooseButtonForChoice(choice: String): InlineKeyboardButton.CallbackData {
    val text = choice + "✅"

    return InlineKeyboardButton.CallbackData(
        text = text,
        callbackData = "${ButtonId.UNCHOOSE.name}$BUTTON_ID_DELIMITER${choice}"
    )
}

private fun createChooseButtonForChoice(choice: String): InlineKeyboardButton.CallbackData {
    val text = choice + "❌"

    return InlineKeyboardButton.CallbackData(
        text = text,
        callbackData = "${ButtonId.CHOOSE.name}$BUTTON_ID_DELIMITER${choice}"
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