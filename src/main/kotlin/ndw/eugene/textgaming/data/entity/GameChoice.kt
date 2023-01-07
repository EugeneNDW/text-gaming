package ndw.eugene.textgaming.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ndw.eugene.textgaming.content.Choice

@Entity
@Table(name = "game_choice")
class GameChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    lateinit var choice: Choice

    @ManyToOne
    @JoinColumn(name = "game_id")
    var gameState: GameState? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameChoice) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return GameState::class.hashCode()
    }
}