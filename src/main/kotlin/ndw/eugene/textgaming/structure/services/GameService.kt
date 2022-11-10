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
        //todo отдать инициацию юзера юзер сервису
        val userState: UserState =
            userService.getUserByIdOrNull(userId) ?: return initUserInLocation(userId, Location.DOCKS)

        return getGameMessage(userState)
    }

    fun initUserInLocation(userId: Long, location: Location): GameMessage {
        //todo отдать инициацию юзера юзер сервису
        val userState = createNewUser(userId, location)
        conversationService.processConversation(userState)

        return getGameMessage(userState)
    }

    private fun createNewUser(userId: Long, startLocation: Location): UserState {
        val conversationStartId = locationService.getLocationData(startLocation).startId
        return userService.createUser(userId, conversationStartId, startLocation)
    }

    fun chooseOption(userId: Long, optionId: String): GameMessage {
        val userState = userService.getUserById(userId)
        val optionUUID = UUID.fromString(optionId)
        conversationService.progressConversation(userState, optionUUID)
        conversationService.processConversation(userState)

        return getGameMessage(userState)
    }

    private fun getGameMessage(userState: UserState): GameMessage {
        val conversationPart = conversationService.getCurrentConversation(userState)
        val options = conversationService.getAvailableOptions(userState)

        return GameMessage(conversationPart, options)
    }
}