package ndw.eugene.textgaming.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener


@Entity
@Table(name = "report")
@EntityListeners(AuditingEntityListener::class)
class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, name = "user_id")
    var userId: Long = 0

    @Column(name = "location")
    var location: String? = null

    @Column(nullable = false, name = "conversation_id")
    var conversationId: Long = 0

    @Column(nullable = false, name = "report_text")
    var reportText: String = ""

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Report) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return Report::class.hashCode()
    }
}