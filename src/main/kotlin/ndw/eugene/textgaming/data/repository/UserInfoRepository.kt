package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.UserInfo
import org.springframework.data.repository.CrudRepository

interface UserInfoRepository : CrudRepository<UserInfo, Long>