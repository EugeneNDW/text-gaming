package ndw.eugene.textgaming

import com.github.kotlintelegrambot.Bot
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class TextGameApplication

fun main(args: Array<String>) {
    val app = runApplication<TextGameApplication>(*args)

    app.getBean(Bot::class.java).startPolling()
}