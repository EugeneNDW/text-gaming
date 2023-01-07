package ndw.eugene.textgaming.loaders

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import ndw.eugene.textgaming.content.ConversationProcessors
import ndw.eugene.textgaming.content.GameCharacter
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
import java.util.UUID
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
            val conversationPart = buildConversationPart(it)

            convById[it.id] = conversationPart
        }

        val convToOption = mutableMapOf<Long, MutableList<Option>>()
        conversation.options.forEach {
            val options = convToOption.getOrPut(it.fromId) { mutableListOf() }
            val option = buildOption(it)
            options.add(option)
        }

        logger.info { "data for $location was built" }
        return LocationData(
            location,
            conversation.conversationParts[0].id,
            convById,
            convToOption
        )
    }

    private fun buildConversationPart(conversationPartDto: ConversationPartDto): ConversationPart {
        val conversationPart = ConversationPart(
            id = conversationPartDto.id,
            character = conversationPartDto.character,
            text = conversationPartDto.text,
            illustration = conversationPartDto.illustration
        )

        if (conversationPartDto.processorId != null && conversationPartDto.processorId.isNotBlank()) {
            conversationPart.executable = conversationProcessors.getProcessorById(conversationPartDto.processorId)
        }

        return conversationPart
    }

    private fun buildOption(optionDto: OptionDto): Option {
        val option = Option(
            uuid = optionDto.uuid,
            fromId = optionDto.fromId,
            toId = optionDto.toId,
            optionText = optionDto.optionText,
        )
        if (optionDto.optionConditionId != null && optionDto.optionConditionId.isNotBlank()) {
            option.condition = conversationProcessors.getOptionConditionById(optionDto.optionConditionId)
        }

        return option
    }
}

@Serializable
private data class Conversation(
    val conversationParts: List<ConversationPartDto>,
    val options: List<OptionDto>
)

@Serializable
private data class ConversationPartDto(
    val id: Long,
    val character: GameCharacter,
    val text: String,
    val processorId: String? = null,
    val illustration: String? = null,
)

@Serializable
private data class OptionDto(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val fromId: Long,
    val toId: Long,
    val optionText: String,
    val optionConditionId: String? = null,
)

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}