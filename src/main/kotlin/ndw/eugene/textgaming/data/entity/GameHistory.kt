package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@Table(name = "game_history")
@EntityListeners(AuditingEntityListener::class)
class GameHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    lateinit var optionId: UUID

    @ManyToOne
    @JoinColumn(name = "game_id")
    var gameState: GameState? = null

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameHistory) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return GameState::class.hashCode()
    }
}