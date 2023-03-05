package ndw.eugene.textgaming.loaders

import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.Arrays

private val logger = KotlinLogging.logger {}

@Service
class IllustrationsLoader {

    @Value("classpath:illustrations/*.png")
    lateinit var illustrations: Array<Resource>

    val illustrationsByName = mutableMapOf<String, ByteArray>()

    fun getIllustration(illustrationName: String?): ByteArray? {
        if (illustrationName.isNullOrBlank()) return null

        return illustrationsByName[illustrationName]
            ?: throw IllegalArgumentException("there is no illustration with name: $illustrationName")
    }

    @PostConstruct
    fun loadIllustrations() {
        Arrays.stream(illustrations).forEach {
            val byteArray = it.inputStream.readBytes()
            val fileName = it.filename ?: throw IllegalArgumentException()
            illustrationsByName[fileName] = byteArray
        }
    }
}