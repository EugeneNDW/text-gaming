package ndw.eugene.textgaming.structure.services

import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.structure.data.GameMessage
import ndw.eugene.textgaming.structure.data.UserState
import java.util.*

class GameService(
    private val userService: UserService,
    private val locationService: LocationService,
    private val conversationService: ConversationService,
) {

    fun initUserIfNotExists(userId: Long): GameMessage {
        var userState = userService.getUserByIdOrNull(userId)

        if (userState == null) {
            userState = createNewUser(userId, Location.DOCKS)
            conversationService.processConversation(userState)
        }

        return getGameMessage(userState)
    }

    fun initUserInLocation(userId: Long, location: Location): GameMessage {
        val userState = createNewUser(userId, location)
        conversationService.processConversation(userState)

        return getGameMessage(userState)
    }

    fun chooseOption(userId: Long, optionId: String): GameMessage {
        val userState = userService.getUserById(userId)
        val optionUUID = UUID.fromString(optionId)
        conversationService.progressConversation(userState, optionUUID)
        conversationService.processConversation(userState)

        return getGameMessage(userState)
    }

    private fun createNewUser(userId: Long, startLocation: Location): UserState {
        val conversationStartId = locationService.locationsByName[startLocation]!!.startId
        return userService.createUser(userId, conversationStartId, startLocation)
    }

    private fun getGameMessage(userState: UserState): GameMessage {
        val conversationPart = conversationService.getCurrentConversation(userState)
        val options = conversationService.getAvailableOptions(userState)

        return GameMessage(conversationPart, options)
    }
}