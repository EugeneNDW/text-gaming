package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "feedback")
@EntityListeners(AuditingEntityListener::class)
class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, name = "user_id")
    var userId: Long = 0

    @Column(nullable = false, name = "feedback_text")
    var feedbackText: String = ""

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Feedback) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return Report::class.hashCode()
    }
}