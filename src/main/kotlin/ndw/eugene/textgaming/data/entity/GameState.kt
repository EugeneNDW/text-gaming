package ndw.eugene.textgaming.data.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import kotlinx.serialization.Serializable
import ndw.eugene.textgaming.content.Location

@Entity
@Table(name = "game_state")
@Serializable
class GameState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, name = "user_id")
    var userId: Long = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "location")
    lateinit var location: Location

    @Column(nullable = false, name = "current_conversation_id")
    var currentConversationId: Long = 0

    @OneToMany(mappedBy = "gameState", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val gameChoices: MutableSet<GameChoice> = mutableSetOf()

    @OneToMany(mappedBy = "gameState", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val gameHistory: MutableSet<GameHistory> = mutableSetOf()

    fun addChoice(choice: GameChoice) {
        choice.gameState = this
        gameChoices.add(choice)
    }

    fun removeChoice(choice: GameChoice) {
        choice.gameState = null
        gameChoices.remove(choice)
    }

    fun addHistory(history: GameHistory) {
        history.gameState = this
        gameHistory.add(history)
    }

    fun removeHistory(history: GameHistory) {
        history.gameState = null
        gameHistory.remove(history)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameState) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return GameState::class.hashCode()
    }
}