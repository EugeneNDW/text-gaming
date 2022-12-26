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
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.structure.data.GameMessage
import ndw.eugene.textgaming.structure.services.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

@Configuration
class TextGameBotConfiguration {

    @Bean
    fun getBot(gameService: GameService): Bot {
        return bot {
            logLevel = LogLevel.All()
            token = System.getProperty("TOKEN")
            dispatch {
                message {
                    logger.info { "update received: $update" }
                }

                command("start_in") {
                    if (!checkAuthorized(message)) {
                        bot.sendMessage(ChatId.fromId(message.chat.id), "знакомы?")
                        return@command
                    }

                    val location = Location.valueOf(args[0].uppercase())
                    val chatId = message.chat.id
                    val result = gameService.startNewGameForUser(chatId, location)

                    sendGameMessage(bot, chatId, result)
                }

                command("start") {
                    if (!checkAuthorized(message)) {
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
                        if (!checkAuthorized(message)) {
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
                        if (!checkAuthorized(message)) {
                            bot.sendMessage(ChatId.fromId(chatId), "знакомы?")
                            return@callbackQuery
                        }

                        val result = gameService.startNewGameForUser(chatId)
                        sendGameMessage(bot, chatId, result)
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
                parseMode = ParseMode.MARKDOWN_V2
            )
        } else {
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = formatResponse,
                replyMarkup = optionButtons,
                parseMode = ParseMode.MARKDOWN_V2
            )
        }
    }

    private fun checkAuthorized(message: Message): Boolean {
        val allowedUsers = listOf(41809406L, 95263058L, 348613726L, 812483294L, 270698064L)

        val userID = message.chat.id
        return allowedUsers.contains(userID)
    }

}