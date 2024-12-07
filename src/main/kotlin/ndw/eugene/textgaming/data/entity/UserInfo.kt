package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*
import ndw.eugene.textgaming.services.Locale
import org.hibernate.annotations.NaturalId
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "user_info")
@EntityListeners(AuditingEntityListener::class)
class UserInfo {
    @Id
    @NaturalId
    @Column(nullable = false, name = "id")
    var id: Long = 0
    var username: String = ""
    var permit: Boolean = true
    var lang: String = Locale.EN.name

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserInfo) return false

        return id != 0L && id == other.id
    }

    override fun hashCode(): Int {
        return UserInfo::class.hashCode()
    }
}