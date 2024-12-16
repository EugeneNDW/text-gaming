package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*
import ndw.eugene.textgaming.services.Locale
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "game_state")
@EntityListeners(AuditingEntityListener::class)
class GameState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, name = "is_ended")
    var isEnded: Boolean = false

    @Column(nullable = false, name = "user_id")
    var userId: Long = 0

    @Column(nullable = false, name = "location")
    lateinit var location: String

    @Column(nullable = false, name = "current_conversation_id")
    var currentConversationId: Long = 0
    var lang: String = Locale.EN.name

    @OneToMany(mappedBy = "gameState", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val gameChoices: MutableSet<GameChoice> = mutableSetOf()

    @OneToMany(mappedBy = "gameState", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val gameHistory: MutableSet<GameHistory> = mutableSetOf()

    @OneToMany(mappedBy = "gameState", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val gameCounters: MutableSet<GameCounter> = mutableSetOf()

    @Embedded
    val auditInfo: AuditInfo = AuditInfo()

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

    fun addCounter(counter: GameCounter) {
        counter.gameState = this
        gameCounters.add(counter)
    }

    fun removeCounter(counter: GameCounter) {
        counter.gameState = null
        gameCounters.remove(counter)
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