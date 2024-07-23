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
import ndw.eugene.textgaming.data.entity.ConversationEntity
import ndw.eugene.textgaming.data.entity.LocationEntity
import ndw.eugene.textgaming.data.entity.OptionEntity
import ndw.eugene.textgaming.data.repository.ConversationRepository
import ndw.eugene.textgaming.data.repository.LocationRepository
import ndw.eugene.textgaming.data.repository.OptionRepository
import ndw.eugene.textgaming.services.ChoiceService
import ndw.eugene.textgaming.services.CounterService
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
    private val choiceService: ChoiceService,
    private val counterService: CounterService,
) {

    @Value("classpath:conversations/*.json")
    lateinit var resources: Array<Resource>

    fun loadLocations() {
        logger.info { "start loading conversations" }
        Arrays.stream(resources)
            .forEach { r ->
                logger.info { "start loading ${r.filename}" }
                val locationName = r.filename?.split(".")?.get(0) ?: throw IllegalArgumentException()
                val text = InputStreamReader(r.inputStream, "UTF-8").use {
                    FileCopyUtils.copyToString(it)
                }
                val conversation = Json.decodeFromString<Conversation>(text)
                if (locationRepository.findByName(locationName.uppercase()) == null) {
                    loadLocation(locationName.uppercase(), conversation)
                }
                logger.info { "${r.filename} was loaded" }
            }
        logger.info { "all conversations were loaded" }
    }

    private fun loadLocation(locationName: String, conversation: Conversation) {
        //save location
        val locationEntity = LocationEntity()
        logger.info { "migrate location: $locationName" }
        locationEntity.name = locationName
        locationEntity.startId = 0
        val savedLocation = locationRepository.save(locationEntity)
        val locationId = savedLocation.id ?: throw IllegalArgumentException("location saved incorrectly")
        logger.info { "location $locationName saved" }
        logger.info { "start saving conversations for location: $locationName" }

        //save conversations
        val conversationIdToId = HashMap<Long, Long>()
        var savedConversationCounter = 0
        conversation.conversationParts.forEach {
            val newConversation = ConversationEntity()
            newConversation.locationId = locationId
            newConversation.person = it.character
            newConversation.conversationText = it.text
            newConversation.illustration = it.illustration ?: ""
            newConversation.processorId = it.processorId ?: ""
            val savedConversation = conversationRepository.save(newConversation)
            conversationIdToId[it.id] = savedConversation.id!!
            savedConversationCounter++
        }
        logger.info { "$savedConversationCounter conversationParts were saved for location $locationName" }

        conversation.conversationParts.stream().map { it.processorId }.filter { !it.isNullOrBlank() }
            .forEach { processor ->
                val (action, value) = processor!!.split(':').let { it[0] to it[1] }
                if (action == "MEMORIZE") {
                    choiceService.createChoiceIfNotExists(value)
                } else if (action == "INCREASE" || action == "DECREASE") {
                    counterService.createCounterIfNotExists(value)
                }
            }
        //save options
        var savedOptionCounter = 0
        conversation.options.forEach {
            val newOption = OptionEntity()
            newOption.fromId = conversationIdToId[it.fromId]
                ?: throw IllegalArgumentException("can't find conversation in saved ids, ABORT")
            newOption.toId = conversationIdToId[it.toId]
                ?: throw IllegalArgumentException("can't find conversation in saved ids, ABORT")
            newOption.optionText = it.optionText
            newOption.optionCondition = it.optionConditionId ?: ""
            newOption.locationId = locationId
            optionRepository.save(newOption)
            savedOptionCounter++
        }

        //update startId for saved location
        savedLocation.startId = conversationIdToId[savedLocation.startId]
            ?: throw IllegalArgumentException("can't find conversation in saved ids, ABORT")
        locationRepository.save(savedLocation)
        logger.info { "$savedOptionCounter options were saved for location $locationName" }
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
    val character: String,
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