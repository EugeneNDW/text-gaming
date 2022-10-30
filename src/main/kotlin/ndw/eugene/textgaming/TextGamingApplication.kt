package ndw.eugene.textgaming

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
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

fun main(arg: Array<String>) {
    println("запустились")
    val gameService = initGameService()
    println("инициализировались и готовы принимать запросы")

    val bot = bot {
        logLevel = LogLevel.Network.Body
        token = ""
        dispatch {

            command("start_in") {
                val location = Location.valueOf(args[0].uppercase())
                val chatId = message.chat.id
                val result = gameService.initUserInLocation(chatId, location)

                sendGameMessage(bot, chatId, result)
            }

            command("start") {
                val chatId = message.chat.id
                val result = gameService.initUserIfNotExists(chatId)

                sendGameMessage(bot, chatId, result)
            }

            callbackQuery {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                val result = gameService.chooseOption(chatId, callbackQuery.data)

                sendGameMessage(bot, chatId, result)
            }
        }
    }

    bot.startPolling()
}

private fun initGameService(): GameService {
    val conversationProcessors = ConversationProcessors()
    val historyService = HistoryService()
    val choiceService = ChoiceService()
    val userService = UserService()

    val conversationLoader = ConversationLoader(conversationProcessors)
    val locationService = LocationService(conversationLoader)
    val conversationService = ConversationService(historyService, locationService)

    conversationProcessors.locationService = locationService
    conversationProcessors.choiceService = choiceService
    conversationProcessors.initProcessors()
    conversationProcessors.initOptionConditions()

    locationService.initLocations()

    return GameService(userService, locationService, conversationService)
}

private fun sendGameMessage(bot: Bot, chatId: Long, message: GameMessage) {
    val availableOptions = message.options.filter { it.available }

    val optionButtons = optionsToButtons(availableOptions)
    bot.sendMessage(
        chatId = ChatId.fromId(chatId),
        text = formatResponse(message.currentConversation, availableOptions),
        replyMarkup = optionButtons,
        parseMode = ParseMode.MARKDOWN_V2
    )
}

private fun formatResponse(conversationPart: ConversationPart, options: List<UserOption>): String {
    var result = ""
    result += prepareForMarkdown("${conversationPart.character}: ${conversationPart.text}")
    result += "\n"
    result += "\n"

    options.forEachIndexed { index, userOption ->
        val text = "${index + 1} : ${userOption.option.optionText}"
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

private fun optionsToButtons(options: List<UserOption>): InlineKeyboardMarkup {
    val buttons: MutableList<InlineKeyboardButton> = mutableListOf()

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

    return InlineKeyboardMarkup.create(buttons)
}