package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "conversations")
@EntityListeners(AuditingEntityListener::class)
class ConversationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var person: String = ""

    @Column(name = "conversation_text", nullable = false)
    var conversationText: String = ""

    @Column(name = "processor_id", nullable = false)
    var processorId: String = ""

    @Column(nullable = false)
    var illustration: String = ""

    @Column(name = "location_id", nullable = false)
    var locationId: Long = 0

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()
}