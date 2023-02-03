package ndw.eugene.textgaming.data.entity

import jakarta.persistence.Column
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "game_counter")
@EntityListeners(AuditingEntityListener::class)
class GameCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "counter_type")
    lateinit var counterType: CounterType

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

enum class CounterType {
    BOY_RELATIONSHIP, BAD_GUY
}