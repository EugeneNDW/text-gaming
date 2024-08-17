package ndw.eugene.textgaming

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.logging.LogLevel
import mu.KotlinLogging
import ndw.eugene.textgaming.data.GameMessage
import ndw.eugene.textgaming.services.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}


@Configuration
class TextGameBotConfiguration {
    @Bean
    fun getBot(
        @Value("\${application.bot.token}") botToken: String,
        @Value("\${application.bot.loglevel}") botLogLevel: String,
        gameService: GameService,
        managerService: ManagerService,
        feedbackService: FeedbackService,
        authService: AuthService,
        choiceService: ChoiceService,
        locationService: LocationService,
    ): Bot {
        return bot {
            logLevel = decideLogLevel(botLogLevel)
            token = botToken

            dispatch {
                //logging
                message {
                    logger.info { "update received: $update" }
                }
                callbackQuery {
                    logger.info { "update received: $update" }
                }

                //authorization
                message {
                    val username = message.from?.username ?: ""
                    val chatId = message.chat.id
                    if (!(authService.checkAuthorized(chatId))) {
                        authService.createUser(chatId, username)
                    }
                }
                callbackQuery {
                    val message = callbackQuery.message
                    val username = callbackQuery.from.username ?: ""
                    val chatId = message?.chat?.id ?: return@callbackQuery

                    if (!(authService.checkAuthorized(chatId))) {
                        authService.createUser(chatId, username)
                    }
                }

                command("copyright") {
                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = COPYRIGHT_MESSAGE,
                        parseMode = ParseMode.HTML
                    )
                }
                command("report") {
                    val reportText = args.joinToString(" ")
                    feedbackService.writeReport(message.chat.id, reportText)

                    bot.sendMessage(ChatId.fromId(message.chat.id), REPORT_RECEIVED_MESSAGE)
                }
                command("feedback") {
                    val feedbackText = args.joinToString(" ")
                    feedbackService.writeFeedback(message.chat.id, feedbackText)

                    bot.sendMessage(ChatId.fromId(message.chat.id), FEEDBACK_RECEIVED_MESSAGE)
                }
                command("start") {
                    val chatId = message.chat.id
                    val responseMessage = if (gameService.userHasGameActive(chatId)) {
                        GAME_STARTED_MESSAGE
                    } else {
                        WELCOME_MESSAGE
                    }

                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = responseMessage,
                        replyMarkup = createStartGameButton(),
                    )
                }

                callbackQuery {
                    if (callbackQuery.data.startsWith(ButtonId.OPTION.name)) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val optionId = callbackQuery.data.split(BUTTON_ID_DELIMITER)[1]
                        val result = gameService.chooseOption(chatId, optionId)

                        editMessage(bot, message)
                        bot.sendGameMessage(chatId, result)
                    }
                }
                callbackQuery {
                    if (callbackQuery.data == ButtonId.START.name) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val result = gameService.startNewGameForUser(chatId)
                        bot.sendGameMessage(chatId, result)
                    }
                }

                //test buttons
                command("users") {
                    if (!checkAdmin(message)) return@command
                    val chatId = message.chat.id
                    var result = ""
                    authService.getAllUsers().forEach { result = result + "," + it.id }
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = result)
                }
                command("start_in") {
                    if (!checkAdmin(message)) return@command
                    val chatId = message.chat.id
                    val result = gameService.startNewGameForUser(chatId, args[0].uppercase())

                    bot.sendGameMessage(chatId, result)
                }
                command("whereami") {
                    if (!checkAdmin(message)) return@command
                    val chatId = message.chat.id
                    val result = gameService.getUserCurrentPlace(chatId)

                    bot.sendGameMessage(chatId, result)
                }
                command("locations") {
                    if (!checkAdmin(message)) return@command
                    val chatId = message.chat.id
                    val gameState = gameService.getUsersCurrentGame(chatId) ?: return@command
                    val buttons = getLocationsButtons(gameState, locationService.findAll())
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "LOCATIONS", replyMarkup = buttons)
                }
                command("choices") {
                    if (!checkAdmin(message)) return@command
                    val chatId = message.chat.id
                    val gameState = gameService.getUsersCurrentGame(chatId) ?: return@command
                    val buttons = getChoicesButtons(gameState, choiceService.getAllChoices())
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "CHOICES", replyMarkup = buttons)
                }
                callbackQuery {
                    if (callbackQuery.data.startsWith(ButtonId.LOCATION.name)) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val locationName = callbackQuery.data.split(BUTTON_ID_DELIMITER)[1]
                        managerService.changeLocation(chatId, locationName)

                        val gameState = gameService.getUsersCurrentGame(chatId) ?: return@callbackQuery
                        val buttons = getLocationsButtons(gameState, locationService.findAll())
                        bot.editMessageText(
                            chatId = ChatId.fromId(chatId),
                            messageId = message.messageId,
                            text = "LOCATIONS",
                            parseMode = ParseMode.HTML,
                            replyMarkup = buttons
                        )
                    }
                }
                callbackQuery {
                    if (callbackQuery.data.startsWith(ButtonId.CHOOSE.name)) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val choiceName = callbackQuery.data.split(BUTTON_ID_DELIMITER)[1]
                        managerService.addChoice(chatId, choiceName)
                        val gameState = gameService.getUsersCurrentGame(chatId) ?: return@callbackQuery
                        val buttons = getChoicesButtons(gameState, choiceService.getAllChoices())

                        bot.editMessageText(
                            chatId = ChatId.fromId(chatId),
                            messageId = message.messageId,
                            text = "CHOICES",
                            parseMode = ParseMode.HTML,
                            replyMarkup = buttons
                        )
                    }
                }
                callbackQuery {
                    if (callbackQuery.data.startsWith(ButtonId.UNCHOOSE.name)) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val choiceName = callbackQuery.data.split(BUTTON_ID_DELIMITER)[1]
                        managerService.removeChoice(chatId, choiceName)
                        val gameState = gameService.getUsersCurrentGame(chatId) ?: return@callbackQuery
                        val buttons = getChoicesButtons(gameState, choiceService.getAllChoices())

                        bot.editMessageText(
                            chatId = ChatId.fromId(chatId),
                            messageId = message.messageId,
                            text = "CHOICES",
                            parseMode = ParseMode.HTML,
                            replyMarkup = buttons
                        )
                    }
                }

            }
        }
    }

    private fun Bot.sendGameMessage(chatId: Long, message: GameMessage) {
        val availableOptions = message.options.filter { it.available }
        val optionButtons = optionsToButtons(availableOptions)
        val formatResponse = formatResponse(message.currentConversation, availableOptions)

        if (message.currentConversation.illustration != null) {
            sendPhoto(
                chatId = ChatId.fromId(chatId),
                photo = TelegramFile.ByByteArray(message.currentConversation.illustration, "illustration.png"),
                caption = formatResponse,
                replyMarkup = optionButtons,
                parseMode = ParseMode.HTML
            )
        } else {
            sendMessage(
                chatId = ChatId.fromId(chatId),
                text = formatResponse,
                replyMarkup = optionButtons,
                parseMode = ParseMode.HTML
            )
        }
    }

    private fun checkAdmin(message: Message): Boolean {
        val allowedUsers = listOf(95263058L, 348613726L)

        val userID = message.chat.id
        return allowedUsers.contains(userID)
    }

    private fun decideLogLevel(logLevel: String): LogLevel {
        return when (logLevel) {
            "none" -> LogLevel.None
            else -> {
                LogLevel.All()
            }
        }
    }
}