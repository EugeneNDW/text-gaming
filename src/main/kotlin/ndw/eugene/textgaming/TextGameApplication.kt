package ndw.eugene.textgaming

import com.github.kotlintelegrambot.Bot
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

private val logger = KotlinLogging.logger {}

@SpringBootApplication
@EnableJpaAuditing
class TextGameApplication

fun main(args: Array<String>) {
    val app = runApplication<TextGameApplication>(*args)

    app.getBean(Bot::class.java).startPolling()
}