package ndw.eugene.textgaming.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "game_history")
class GameHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    lateinit var optionId: UUID

    @ManyToOne
    @JoinColumn(name = "game_id")
    var gameState: GameState? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameHistory) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return GameState::class.hashCode()
    }
}