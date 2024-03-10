package ndw.eugene.textgaming.controllers

import ndw.eugene.textgaming.services.GameService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class GameController(private val gameService: GameService) {
    @PostMapping("/locations")
    fun createLocation(
        @RequestBody createLocationRequest: CreateLocationRequest
    ): CreateLocationResponse {
        return gameService.createLocation(createLocationRequest)
    }

    @PostMapping("/conversations")
    fun createConversation(
        @RequestBody createConversationRequest: CreateConversationRequest
    ): CreateConversationResponse {
        return gameService.createConversation(createConversationRequest)
    }

    @PostMapping("/options")
    fun addOptions(
        @RequestBody options: List<OptionRequest>
    ): List<OptionResponse> {
        return gameService.createOptions(options)
    }
}

data class CreateLocationRequest(val name: String, val startId: Long)
data class CreateLocationResponse(val id: Long, val name: String, val startId: Long)
data class CreateConversationRequest(
    val conversationId: Long,
    val person: String,
    val conversationText: String,
    val processorId: String,
    val illustration: String,
    val locationId: Long
)

data class CreateConversationResponse(
    val id: Long,
    val conversationId: Long,
    val person: String,
    val conversationText: String,
    val processorId: String,
    val illustration: String,
    val locationId: Long
)

data class OptionRequest(
    val fromId: Long,
    val toId: Long,
    val optionText: String,
    val optionConditionId: String,
    val locationId: Long
)

data class OptionResponse(
    val id: UUID,
    val fromId: Long,
    val toId: Long,
    val optionText: String,
    val optionConditionId: String,
    val locationId: Long
)

