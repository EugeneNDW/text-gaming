package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "choices")
class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, name = "name")
    lateinit var name: String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Choice) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return Choice::class.hashCode()
    }
}