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
                    if (!checkAuthorized(message)) {
                        bot.sendMessage(ChatId.fromId(message.chat.id), "знакомы?")
                        update.consume()
                    }
                }
                callbackQuery {
                    val message = callbackQuery.message
                    val chatId = message?.chat?.id ?: return@callbackQuery
                    if (!checkAuthorized(message)) {
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

                command("start_in") {
                    val location = Location.valueOf(args[0].uppercase())
                    val chatId = message.chat.id
                    val result = gameService.startNewGameForUser(chatId, location)

                    sendGameMessage(bot, chatId, result)
                }

                command("start") {
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
                    if (callbackQuery.data != "START_GAME"
                        && !callbackQuery.data.startsWith("choose")
                        && !callbackQuery.data.startsWith("unchoose")
                        && !callbackQuery.data.startsWith("location")
                    ) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val result = gameService.chooseOption(chatId, callbackQuery.data)

                        editMessage(bot, message)
                        sendGameMessage(bot, chatId, result)
                    }
                }

                callbackQuery {
                    if (callbackQuery.data == "START_GAME") {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val result = gameService.startNewGameForUser(chatId)
                        sendGameMessage(bot, chatId, result)
                    }
                }


                //test buttons

                command("whereami") {
                    val chatId = message.chat.id
                    val result = gameService.getUserCurrentPlace(chatId)

                    sendGameMessage(bot, chatId, result)
                }
                command("locations") {
                    val chatId = message.chat.id
                    val gameState = gameService.getUsersCurrentGame(chatId) ?: return@command
                    val buttons = getLocationsButtons(gameState)
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "LOCATIONS", replyMarkup = buttons)
                }
                command("choices") {
                    val chatId = message.chat.id
                    val gameState = gameService.getUsersCurrentGame(chatId) ?: return@command
                    val buttons = getChoicesButtons(gameState)
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "CHOICES", replyMarkup = buttons)
                }

                callbackQuery {
                    if (callbackQuery.data.startsWith("location")) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val locationName = callbackQuery.data.split(":")[1]
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
                    if (callbackQuery.data.startsWith("choose")) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val choiceName = callbackQuery.data.split(":")[1]
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
                    if (callbackQuery.data.startsWith("unchoose")) {
                        val message = callbackQuery.message
                        val chatId = message?.chat?.id ?: return@callbackQuery
                        val choiceName = callbackQuery.data.split(":")[1]
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

    private fun sendGameMessage(bot: Bot, chatId: Long, message: GameMessage) {
        val availableOptions = message.options.filter { it.available }
        val optionButtons = optionsToButtons(availableOptions)
        val formatResponse = formatResponse(message.currentConversation, availableOptions)
        val illustration = message.currentConversation.illustration

        if (illustration != null && illustration.isNotBlank()) {
            bot.sendPhoto(
                chatId = ChatId.fromId(chatId),
                photo = TelegramFile.ByByteArray(readBytesFromFile(illustration), illustration),
                caption = formatResponse,
                replyMarkup = optionButtons,
                parseMode = ParseMode.HTML
            )
        } else {
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = formatResponse,
                replyMarkup = optionButtons,
                parseMode = ParseMode.HTML
            )
        }
    }

    private fun checkAuthorized(message: Message): Boolean {
        val allowedUsers = listOf(95263058L)

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