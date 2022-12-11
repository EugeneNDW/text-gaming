package ndw.eugene.textgaming

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import ndw.eugene.textgaming.content.ConversationProcessors
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.structure.services.LocationService
import ndw.eugene.textgaming.structure.data.ConversationPart
import ndw.eugene.textgaming.structure.data.GameMessage
import ndw.eugene.textgaming.structure.data.UserOption
import ndw.eugene.textgaming.structure.services.*
import ndw.eugene.textgaming.utils.ConversationLoader
import java.io.File

class TextGameApplication

fun main() {
    println("запустились")
    val gameService = initGameService()
    println("инициализировались и готовы принимать запросы")

    val bot = bot {
        logLevel = LogLevel.Network.Basic
        token = System.getenv("TOKEN")
        dispatch {
            command("start_in") {
                if(!checkAuthorized(message)) {
                    bot.sendMessage(ChatId.fromId(message.chat.id), "знакомы?")
                    return@command
                }

                val location = Location.valueOf(args[0].uppercase())
                val chatId = message.chat.id
                val result = gameService.startNewGameForUser(chatId, location)

                sendGameMessage(bot, chatId, result)
            }

            command("start") {
                if(!checkAuthorized(message)) {
                    bot.sendMessage(ChatId.fromId(message.chat.id), "знакомы?")
                    return@command
                }

                val chatId = message.chat.id
                val responseMessage = if (gameService.userHasGameStarted(chatId)) {
                    "У вас уже есть запущенная игра, прогресс в ней будет утерян. Перезапустить?"
                } else {
                    "Добро пожаловать, чтобы начать играть нажмите кнопку"
                }

                val startGameButton = createStartGameButton()
                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = responseMessage,
                    replyMarkup = startGameButton,
                )
            }

            callbackQuery {
                if (callbackQuery.data != "START_GAME") {
                    val message = callbackQuery.message
                    val chatId = message?.chat?.id ?: return@callbackQuery
                    if(!checkAuthorized(message)) {
                        bot.sendMessage(ChatId.fromId(chatId), "знакомы?")
                        return@callbackQuery
                    }

                    val result = gameService.chooseOption(chatId, callbackQuery.data)

                    editMessage(bot, message)
                    sendGameMessage(bot, chatId, result)
                }
            }

            callbackQuery {
                if (callbackQuery.data == "START_GAME") {
                    val message = callbackQuery.message
                    val chatId = message?.chat?.id ?: return@callbackQuery
                    if(!checkAuthorized(message)) {
                        bot.sendMessage(ChatId.fromId(chatId), "знакомы?")
                        return@callbackQuery
                    }

                    val result = gameService.startNewGameForUser(chatId)
                    sendGameMessage(bot, chatId, result)
                }
            }
        }
    }

    bot.startPolling()
}
fun checkAuthorized(message: Message): Boolean {
    val allowedUsers = listOf(1L)

    val userID = message.chat.id
    return allowedUsers.contains(userID)
}

private fun removeButtons(bot: Bot, message: Message) {
    bot.editMessageReplyMarkup(
        chatId = ChatId.fromId(message.chat.id),
        messageId = message.messageId,
    )
}

private fun editMessage(bot: Bot, message: Message) {
    if (message.text != null) {
        val newText = editText(message.text)

        bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = newText,
            parseMode = ParseMode.MARKDOWN_V2
        )
    } else if (message.caption != null) {
        val newText = editText(message.caption)

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

private fun editText(text: String?): String {
    val messageTokens = text!!.split("\n\n")
    val messageWithoutTokens = messageTokens[0]
    val messageWithoutOptionsTokens = messageWithoutTokens.split("\n")
    return makeTextBold(prepareForMarkdown(messageWithoutOptionsTokens[0])) + " \n" + prepareForMarkdown(
        messageWithoutOptionsTokens[1]
    )
}

private fun initGameService(): GameService {
    val conversationProcessors = ConversationProcessors()
    val historyService = HistoryService()
    val choiceService = ChoiceService()
    val conversationLoader = ConversationLoader(conversationProcessors)
    val locationService = LocationService(conversationLoader)
    val conversationService = ConversationService(historyService, locationService)

    conversationProcessors.locationService = locationService
    conversationProcessors.choiceService = choiceService
    conversationProcessors.initProcessors()
    conversationProcessors.initOptionConditions()

    locationService.initLocations()

    return GameService(locationService, conversationService)
}

private fun sendGameMessage(bot: Bot, chatId: Long, message: GameMessage) {
    val availableOptions = message.options.filter { it.available }
    val optionButtons = optionsToButtons(availableOptions)

    if (message.currentConversation.illustration != null) {
        val fileContent =
            TextGameApplication::class.java.getResource("/illustrations/${message.currentConversation.illustration}") //todo вынести загрузку файла в отдельный сервис
        val file = File(fileContent.toURI())
        bot.sendPhoto(
            chatId = ChatId.fromId(chatId),
            photo = TelegramFile.ByFile(file),
            caption = formatResponse(message.currentConversation, availableOptions),
            replyMarkup = optionButtons,
            parseMode = ParseMode.MARKDOWN_V2
        )
    } else {
        bot.sendMessage(
            chatId = ChatId.fromId(chatId),
            text = formatResponse(message.currentConversation, availableOptions),
            replyMarkup = optionButtons,
            parseMode = ParseMode.MARKDOWN_V2
        )
    }
}

private fun formatResponse(conversationPart: ConversationPart, options: List<UserOption>): String {
    var result = ""
    result += makeTextBold(prepareForMarkdown("${conversationPart.character}:")) + "\n" + prepareForMarkdown(
        conversationPart.text
    )
    result += "\n"
    result += "\n"

    if (options.size == 1 && options[0].option.optionText == "...") {
        return result
    }

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

    return result
}

private fun prepareForMarkdown(text: String): String {
    var result = text.replace("_", "\\_")

    result = result.replace("*", "\\*")
    result = result.replace("[", "\\[")
    result = result.replace("]", "\\]")
    result = result.replace("(", "\\(")
    result = result.replace(")", "\\)")
    result = result.replace("~", "\\~")
    result = result.replace("`", "\\`")
    result = result.replace(">", "\\>")
    result = result.replace("#", "\\#")
    result = result.replace("+", "\\+")
    result = result.replace("-", "\\-")
    result = result.replace("=", "\\=")
    result = result.replace("|", "\\|")
    result = result.replace("{", "\\{")
    result = result.replace("}", "\\}")
    result = result.replace(".", "\\.")
    result = result.replace("!", "\\!")

    return result
}

private fun makeTextBold(text: String): String {
    return "*$text*"
}

private fun makeStrikeThrough(text: String): String {
    return "~$text~"
}
private fun createStartGameButton(): InlineKeyboardMarkup {
    val buttons: MutableList<InlineKeyboardButton> = mutableListOf()

    buttons.add(InlineKeyboardButton.CallbackData(
        text = "start new game",
        callbackData = "START_GAME"
    ))

    return InlineKeyboardMarkup.create(buttons)
}

private fun optionsToButtons(options: List<UserOption>): InlineKeyboardMarkup {
    val buttons: MutableList<InlineKeyboardButton> = mutableListOf()

    if (options.size == 1 && options[0].option.optionText == "...") {
        buttons.add(
            InlineKeyboardButton.CallbackData(
                text = "continue...",
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