package ndw.eugene.textgaming.structure.services

import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.structure.data.UserState

class UserService {
    private val users: MutableMap<Long, UserState> = mutableMapOf() //todo положить в базу
    //todo помимо юзер стейта хранить ещё юзер сеттингс, возможно отдельный сервис для настроек, тоже хранить в базе.

    fun getUserById(id: Long): UserState {
        return getUserByIdOrNull(id) ?: throw IllegalArgumentException()
    }

    fun getUserByIdOrNull(id: Long): UserState? {
        return users[id]
    }

    fun createUser(id: Long, currentConversation: Long, startLocation: Location): UserState {
        users[id] = UserState(id, currentConversation, startLocation)

        return users[id] ?: throw IllegalArgumentException()
    }
}