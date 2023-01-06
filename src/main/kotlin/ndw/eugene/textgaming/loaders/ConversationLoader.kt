package ndw.eugene.textgaming.loaders

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import ndw.eugene.textgaming.content.ConversationProcessors
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.data.ConversationPart
import ndw.eugene.textgaming.data.LocationData
import ndw.eugene.textgaming.data.Option
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader
import java.util.Arrays
import java.util.stream.Collectors.toList

private val logger = KotlinLogging.logger {}

@Component
class ConversationLoader(private val conversationProcessors: ConversationProcessors) {

    @Value("classpath:conversations/*.json")
    lateinit var resources: Array<Resource>

    fun loadLocations(): Map<Location, LocationData> {
        logger.info { "start loading conversations" }

        val result = Arrays
            .stream(resources)
            .map { r ->
                logger.info { "start loading ${r.filename}" }
                val locationName = r.filename?.split(".")?.get(0) ?: throw IllegalArgumentException()
                val location = Location.valueOf(locationName.uppercase())
                val text = InputStreamReader(r.inputStream, "UTF-8").use {
                    FileCopyUtils.copyToString(it)
                }
                val conversation = Json.decodeFromString<Conversation>(text)

                val locationData = buildLocationData(conversation, location)
                logger.info { "${r.filename} was loaded" }
                locationData
            }
            .collect(toList())
            .associateBy({ it.location }, { it })

        logger.info { "all conversations were loaded" }
        return result
    }

    private fun buildLocationData(conversation: Conversation, location: Location): LocationData {
        logger.info { "building data for $location" }
        val convById = mutableMapOf<Long, ConversationPart>()
        conversation.conversationParts.forEach {
            if (it.processorId != null && it.processorId.isNotBlank()) {
                it.executable = conversationProcessors.getProcessorById(it.processorId)
            }
            convById[it.id] = it
        }

        val convToOption = mutableMapOf<Long, MutableList<Option>>()
        conversation.options.forEach {
            val options = convToOption.getOrPut(it.fromId) {
                mutableListOf()
            }
            if (it.optionConditionId != null && it.optionConditionId.isNotBlank()) {
                it.condition = conversationProcessors.getOptionConditionById(it.optionConditionId)
            }
            options.add(it)
        }

        logger.info { "data for $location was built" }
        return LocationData(
            location,
            conversation.conversationParts[0].id,
            convById,
            convToOption
        )
    }
}

@Serializable
private data class Conversation(
    val conversationParts: List<ConversationPart>,
    val options: List<Option>
)