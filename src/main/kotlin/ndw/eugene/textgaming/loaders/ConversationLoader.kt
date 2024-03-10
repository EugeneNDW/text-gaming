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
import ndw.eugene.textgaming.content.GameCharacter
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.data.entity.ConversationEntity
import ndw.eugene.textgaming.data.entity.LocationEntity
import ndw.eugene.textgaming.data.entity.OptionEntity
import ndw.eugene.textgaming.data.repository.ConversationRepository
import ndw.eugene.textgaming.data.repository.LocationRepository
import ndw.eugene.textgaming.data.repository.OptionRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader
import java.util.Arrays
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Component
class ConversationLoader(
    private val locationRepository: LocationRepository,
    private val conversationRepository: ConversationRepository,
    private val optionRepository: OptionRepository,
) {

    @Value("classpath:conversations/*.json")
    lateinit var resources: Array<Resource>

    fun loadLocations() {
        logger.info { "start loading conversations" }
        Arrays.stream(resources)
            .forEach { r ->
                logger.info { "start loading ${r.filename}" }
                val locationName = r.filename?.split(".")?.get(0) ?: throw IllegalArgumentException()
                val location = Location.valueOf(locationName.uppercase())
                val text = InputStreamReader(r.inputStream, "UTF-8").use {
                    FileCopyUtils.copyToString(it)
                }
                val conversation = Json.decodeFromString<Conversation>(text)
                logger.info { "${r.filename} was loaded" }
                if (locationRepository.findByName(location.name) == null) {
                    migrateLocationToDataBase(location, conversation)
                }
            }
        logger.info { "all conversations were loaded" }
    }

    private fun migrateLocationToDataBase(location: Location,  conversation: Conversation) {
        val locationEntity = LocationEntity()
        logger.info { "migrate location: ${location.name}" }
        locationEntity.name = location.name
        locationEntity.startId = 0
        val savedLocation = locationRepository.save(locationEntity)
        val locationId = savedLocation.id ?: throw IllegalArgumentException("location saved incorrectly")
        logger.info { "location ${location.name} saved" }
        logger.info { "start saving conversations for location: ${location.name}" }

        val conversationIdToId = HashMap<Long, Long>()
        var savedConversationCounter = 0
        conversation.conversationParts.forEach {
            val newConversation = ConversationEntity()
            newConversation.locationId = locationId
            newConversation.person = it.character.name
            newConversation.conversationText = it.text
            newConversation.illustration = it.illustration ?: ""
            newConversation.processorId = it.processorId ?: ""
            val savedConversation = conversationRepository.save(newConversation)
            conversationIdToId[it.id] = savedConversation.id!!
            savedConversationCounter++
        }
        logger.info { "$savedConversationCounter conversationParts were saved for location ${location.name}" }

        var savedOptionCounter = 0
        conversation.options.forEach {
            val newOption = OptionEntity()
            newOption.fromId = conversationIdToId[it.fromId] ?: throw IllegalArgumentException("can't find conversation in saved ids, ABORT")
            newOption.toId = conversationIdToId[it.toId] ?: throw IllegalArgumentException("can't find conversation in saved ids, ABORT")
            newOption.optionText = it.optionText
            newOption.optionConditionId = it.optionConditionId ?: ""
            newOption.locationId = locationId
            optionRepository.save(newOption)
            savedOptionCounter++
        }

        savedLocation.startId = conversationIdToId[savedLocation.startId] ?: throw IllegalArgumentException("can't find conversation in saved ids, ABORT")
        locationRepository.save(savedLocation)
        logger.info { "$savedOptionCounter options were saved for location ${location.name}" }
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