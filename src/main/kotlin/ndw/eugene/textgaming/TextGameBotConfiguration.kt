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
import ndw.eugene.textgaming.content.Choice
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.data.GameMessage
import ndw.eugene.textgaming.services.FeedbackService
import ndw.eugene.textgaming.services.GameService
import ndw.eugene.textgaming.services.ManagerService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

private val authorizedUsers = mutableListOf<String>()

@Configuration
class TextGameBotConfiguration {
    @Bean
    fun getBot(
        @Value("\${application.bot.token}") botToken: String,
        @Value("\${application.bot.loglevel}") botLogLevel: String,
        gameService: GameService,
        managerService: ManagerService,
        feedbackService: FeedbackService
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
                    val username = update.message?.from?.username ?: return@message

                    if (!(checkAdmin(message) || checkAuthorized(username))) {
                        bot.sendMessage(ChatId.fromId(message.chat.id), "знакомы?")
                        update.consume()
                    }
                }
                callbackQuery {
                    val message = callbackQuery.message
                    val username = callbackQuery.from.username ?: return@callbackQuery
                    val chatId = message?.chat?.id ?: return@callbackQuery

                    if (!(checkAdmin(message) || checkAuthorized(username))) {
                        bot.sendMessage(ChatId.fromId(chatId), "знакомы?")
                        update.consume()
                    }
                }

                command("report") {
                    val reportText = args.joinToString(" ")
                    feedbackService.writeReport(message.chat.id, reportText)

                    bot.sendMessage(ChatId.fromId(message.chat.id), "we got your report and we'll fix it asap")
                }
                command("feedback") {
                    val feedbackText = args.joinToString(" ")
                    feedbackService.writeFeedback(message.chat.id, feedbackText)

                    bot.sendMessage(ChatId.fromId(message.chat.id), "ty for feedback <3")
                }
                command("start") {
                    val chatId = message.chat.id
                    val responseMessage = if (gameService.userHasGameActive(chatId)) {
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
                command("start_in") {
                    if (!checkAdmin(message)) return@command
                    val location = Location.valueOf(args[0].uppercase())
                    val chatId = message.chat.id
                    val result = gameService.startNewGameForUser(chatId, location)

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
                    val buttons = getLocationsButtons(gameState)
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "LOCATIONS", replyMarkup = buttons)
                }
                command("choices") {
                    if (!checkAdmin(message)) return@command
                    val chatId = message.chat.id
                    val gameState = gameService.getUsersCurrentGame(chatId) ?: return@command
                    val buttons = getChoicesButtons(gameState)
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "CHOICES", replyMarkup = buttons)
                }
                command("permit") {
                    if (!checkAdmin(message)) return@command
                    val chatId = message.chat.id
                    val username = args[0]

                    authorizedUsers.add(username)
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "open service for user with username: $username")
                }
                command("forbid") {
                    if (!checkAdmin(message)) return@command
                    val chatId = message.chat.id
                    val username = args[0]

                    authorizedUsers.remove(username)
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "close service for user with username: $username")
                }
                callbackQuery {
                    if (callbackQuery.data.startsWith(ButtonId.LOCATION.name)) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val locationName = callbackQuery.data.split(BUTTON_ID_DELIMITER)[1]
                        managerService.changeLocation(chatId, locationName)

                        val gameState = gameService.getUsersCurrentGame(chatId) ?: return@callbackQuery
                        val buttons = getLocationsButtons(gameState)
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
                        managerService.addChoice(chatId, Choice.valueOf(choiceName))
                        val gameState = gameService.getUsersCurrentGame(chatId) ?: return@callbackQuery
                        val buttons = getChoicesButtons(gameState)

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
                        managerService.removeChoice(chatId, Choice.valueOf(choiceName))
                        val gameState = gameService.getUsersCurrentGame(chatId) ?: return@callbackQuery
                        val buttons = getChoicesButtons(gameState)

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

    private fun checkAuthorized(username: String): Boolean {
        return authorizedUsers.contains(username)
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