package ndw.eugene.textgaming.services

import ndw.eugene.textgaming.data.entity.UserInfo
import ndw.eugene.textgaming.data.repository.UserInfoRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userInfoRepository: UserInfoRepository) {

    fun checkAuthorized(id: Long): Boolean {
        val user = userInfoRepository.findById(id)
        if (user.isPresent) {
            return user.get().permit
        }
        return false
    }

    fun setLanguage(id: Long, locale: Locale) {
        val user = userInfoRepository.findById(id).orElseThrow { IllegalArgumentException() }
        user.lang = locale.name
        userInfoRepository.save(user)
    }

    fun getUser(id: Long): UserInfo {
        return userInfoRepository.findById(id).orElseThrow { IllegalArgumentException() }
    }

    fun createUser(userId: Long, username: String) {
        val userInfo = UserInfo()
        userInfo.id = userId
        userInfo.username = username
        userInfo.permit = true
        userInfoRepository.save(userInfo)
    }

    fun getAllUsers(): Iterable<UserInfo> {
        return userInfoRepository.findAll()
    }
}