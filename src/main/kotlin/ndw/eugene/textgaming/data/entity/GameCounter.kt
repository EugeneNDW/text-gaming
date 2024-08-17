package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "game_counter")
@EntityListeners(AuditingEntityListener::class)
class GameCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "counter_id", nullable = false)
    lateinit var counter: Counter

    @Column(nullable = false, name = "counter_value")
    var counterValue: Int = 0

    @ManyToOne
    @JoinColumn(name = "game_id")
    var gameState: GameState? = null

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameCounter) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return GameCounter::class.hashCode()
    }
}