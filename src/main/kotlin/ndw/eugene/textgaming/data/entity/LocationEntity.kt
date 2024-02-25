package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "locations")
@EntityListeners(AuditingEntityListener::class)
class LocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var name: String = ""

    @Column(name = "start_id", nullable = false)
    var startId: Long = 0

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()
}