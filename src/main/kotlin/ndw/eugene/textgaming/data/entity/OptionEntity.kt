package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@Table(name = "options")
@EntityListeners(AuditingEntityListener::class)
class OptionEntity {
    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(name = "from_id", nullable = false)
    var fromId: Long = 0

    @Column(name = "to_id", nullable = false)
    var toId: Long = 0

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = [CascadeType.ALL])
    @JoinColumn(name = "text_id", nullable = false)
    var text: TextEntity? = null

    @Column(name = "option_condition", nullable = false)
    var optionCondition: String = ""

    @Column(name = "location_id", nullable = false)
    var locationId: Long = 0

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()
}