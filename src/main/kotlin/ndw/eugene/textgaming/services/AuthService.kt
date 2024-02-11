package ndw.eugene.textgaming.services

import ndw.eugene.textgaming.data.entity.UserInfo
import ndw.eugene.textgaming.data.repository.UserInfoRepository
import org.springframework.boot.autoconfigure.security.SecurityProperties.User
import org.springframework.stereotype.Service

@Service
class AuthService(private val userInfoRepository: UserInfoRepository) {

    fun checkAuthorized(id: Long): Boolean {
        val user = userInfoRepository.findById(id)
        if (user.isPresent) {
            return user.get().permit
        }
        return false
    }

    fun createUser(userId: Long, username: String) {
        val userInfo = UserInfo()
        userInfo.id = userId
        userInfo.username = username
        userInfo.permit = true
        userInfoRepository.save(userInfo)
    }

    fun removeUser(userId: Long) {
        userInfoRepository.deleteById(userId)
    }

    fun getAllUsers(): Iterable<UserInfo> {
        return userInfoRepository.findAll();
    }
}