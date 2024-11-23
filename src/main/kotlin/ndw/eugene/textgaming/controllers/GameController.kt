package ndw.eugene.textgaming.controllers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import ndw.eugene.textgaming.services.GameService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@CrossOrigin(origins = ["http://localhost:3000"])
@RequestMapping("/games/1")
class GameController(private val gameService: GameService) {

    @PostMapping("/choices")
    fun addChoices(
        @RequestBody choice: ChoiceRequest
    ): ChoiceResponse {
        val savedChoice = gameService.createChoices(choice)
        return ChoiceResponse(savedChoice.id!!, savedChoice.name)
    }

    @GetMapping("/choices")
    fun getChoices(): List<ChoiceResponse> {
        val choices = gameService.getAllChoices()
        return choices.map { choice ->
            ChoiceResponse(choice.id!!, choice.name)
        }
    }

    @PostMapping("/counters")
    fun addCounters(
        @RequestBody counter: CounterRequest
    ): CounterResponse {
        val counterEntity = gameService.createCounter(counter)
        return CounterResponse(counterEntity.id!!, counter.name)
    }

    @GetMapping("/counters")
    fun getCounters(): List<CounterResponse> {
        val counters = gameService.getAllCounters()
        return counters.map { counter ->
            CounterResponse(counter.id!!, counter.name)
        }
    }

    @PostMapping("/locations")
    fun createLocation(
        @RequestBody locationRequest: LocationRequest
    ): LocationResponse {
        val locationEntity = gameService.createLocation(locationRequest)
        return LocationResponse(locationEntity.id!!, locationEntity.name, locationEntity.startId)
    }

    @GetMapping("/locations")
    fun getLocations(): List<LocationResponse> {
        val locations = gameService.getAllLocations()
        return locations.map { location ->
            LocationResponse(location.id!!, location.name, location.startId)
        }
    }

    @PostMapping("/locations/{locationId}/conversations/{conversationId}/options")
    fun createOptionToConversation(
        @PathVariable locationId: Long,
        @PathVariable conversationId: Long,
        @RequestBody request: CreateLinkRequest
    ) {
        gameService.createOption(locationId, conversationId, request)
    }

    @PostMapping("/locations/{locationId}/conversations")
    fun createConversation(
        @PathVariable locationId: Long,
        @RequestBody conversationRequest: ConversationRequest
    ): ConversationResponse {
        val conversationEntity = gameService.createConversation(locationId, conversationRequest)
        return ConversationResponse(
            conversationEntity.id!!,
            conversationEntity.character!!.name,
            conversationEntity.conversationText,
            conversationEntity.processorId,
            conversationEntity.illustration,
            conversationEntity.locationId
        )
    }

    @GetMapping("/locations/{locationId}/conversations")
    fun getConversationsByLocation(
        @PathVariable locationId: Long
    ): List<ConversationResponse> {
        val conversations = gameService.getConversationsByLocation(locationId)
        return conversations.map { conversation ->
            ConversationResponse(
                conversation.id!!,
                conversation.character!!.name,
                conversation.conversationText,
                conversation.processorId,
                conversation.illustration,
                conversation.locationId
            )
        }
    }

    @GetMapping("/locations/{locationId}/options")
    fun getOptionsByLocation(
        @PathVariable locationId: Long
    ): List<OptionResponse> {
        val options = gameService.getOptionsByLocation(locationId)
        return options.map { option ->
            OptionResponse(
                option.id!!,
                option.fromId,
                option.toId,
                option.optionText,
                option.optionCondition,
                option.locationId,
                null
            )
        }
    }

    @GetMapping("/locations/{locationId}/conversations/{conversationId}/options")
    fun getOptionsByConversationId(
        @PathVariable locationId: Long,
        @PathVariable conversationId: Long
    ): List<OptionResponse> {
        return gameService.getOptionsByConversationId(locationId, conversationId)
    }

    @PostMapping("/characters")
    fun createCharacter(
        @RequestBody request: CharacterRequest
    ): CharacterResponse {
        return gameService.createCharacter(request)
    }

    @GetMapping("/characters")
    fun getAllCharacters(): List<CharacterResponse> {
        return gameService.getAllCharacters()
    }
}

data class CreateLinkRequest(
    val conversationRequest: ConversationRequest?,
    val optionRequest: OptionRequest,
    val toConversationId: Long?
)

data class LocationRequest(val name: String, val firstConversationPart: ConversationRequest)
data class LocationResponse(val id: Long, val name: String, val startId: Long)
data class ConversationRequest(
    val person: String,
    val conversationText: String,
    val processorId: String,
    val illustration: String,
)

data class OptionRequest(
    val optionText: String,
    val optionConditionId: String,
)

data class ConversationResponse(
    val id: Long,
    val person: String,
    val conversationText: String,
    val processorId: String,
    val illustration: String,
    val locationId: Long
)

data class OptionResponse(
    val id: UUID,
    val fromId: Long,
    val toId: Long,
    val optionText: String,
    val optionConditionId: String,
    val locationId: Long,
    val toConversation: ConversationResponse?
)

data class ChoiceRequest @JsonCreator constructor(
    @JsonProperty val name: String
)

data class ChoiceResponse(val id: Long, val name: String)

data class CounterRequest @JsonCreator constructor(
    @JsonProperty val name: String
)

data class CounterResponse(val id: Long, val name: String)

data class CharacterRequest @JsonCreator constructor(
    @JsonProperty val name: String
)

data class CharacterResponse(
    val id: Long,
    val name: String
)

