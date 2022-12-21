package ndw.eugene.textgaming.loaders

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import ndw.eugene.textgaming.content.ConversationProcessors
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.structure.data.ConversationPart
import ndw.eugene.textgaming.structure.data.LocationData
import ndw.eugene.textgaming.structure.data.Option
import ndw.eugene.textgaming.walk
import java.nio.file.Files
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}

class ConversationLoader(private val conversationProcessors: ConversationProcessors) {

    fun loadLocations(): MutableMap<Location, LocationData> {
        logger.info { "start loading conversations" }
        val locations: MutableMap<Location, LocationData> = mutableMapOf()

        val fileURL = ConversationLoader::class.java.classLoader.getResource("conversations")
            ?: throw IllegalArgumentException("cant find conversations")

        walk("conversations", fileURL)?.forEach { f ->
            if (Files.isRegularFile(f)) {
                logger.info { "start loading conversation ${f.name}" }
                val locationName = f.name.split(".")[0]
                val conversation = loadConversation(locationName)
                val location = Location.valueOf(locationName.uppercase())

                val locationData = buildLocationData(conversation, location)

                locations[location] = locationData
                logger.info { "conversation ${f.name} was loaded" }
            }
        }

        logger.info { "all conversations were loaded" }
        return locations
    }

    private fun buildLocationData(conversation: Conversation, location: Location): LocationData {
        val convById = mutableMapOf<Long, ConversationPart>()
        conversation.conversationParts.forEach {
            if (it.processorId != null) {
                it.executable = conversationProcessors.getProcessorById(it.processorId)
            }
            convById[it.id] = it
        }

        val convToOption = mutableMapOf<Long, MutableList<Option>>()
        conversation.options.forEach {
            val options = convToOption.getOrPut(it.fromId) {
                mutableListOf()
            }
            if (it.optionConditionId != null) {
                it.condition = conversationProcessors.getOptionConditionById(it.optionConditionId)
            }
            options.add(it)
        }

        return LocationData(
            location,
            conversation.conversationParts[0].id,
            convById,
            convToOption
        )
    }

    private fun loadConversation(locationName: String): Conversation {
        val text =
            ConversationLoader::class.java.classLoader.getResource("conversations/$locationName.json")?.readText()

        return Json.decodeFromString(text ?: "")
    }
}

@Serializable
private data class Conversation(
    val conversationParts: List<ConversationPart>,
    val options: List<Option>
)