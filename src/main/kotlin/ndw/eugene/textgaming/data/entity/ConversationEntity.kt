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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "character_id", nullable = false)
    var character: CharacterEntity? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = [CascadeType.ALL])
    @JoinColumn(name = "text_id", nullable = false)
    var text: TextEntity? = null

    @Column(name = "processor_id", nullable = false)
    var processorId: String = ""

    @Column(nullable = false)
    var illustration: String = ""

    @Column(name = "location_id", nullable = false)
    var locationId: Long = 0

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()
}