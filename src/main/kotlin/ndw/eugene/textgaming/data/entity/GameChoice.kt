package ndw.eugene.textgaming.data.entity

import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ndw.eugene.textgaming.content.Choice
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "game_choice")
@EntityListeners(AuditingEntityListener::class)
class GameChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    lateinit var choice: Choice

    @ManyToOne
    @JoinColumn(name = "game_id")
    var gameState: GameState? = null

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameChoice) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return GameState::class.hashCode()
    }
}