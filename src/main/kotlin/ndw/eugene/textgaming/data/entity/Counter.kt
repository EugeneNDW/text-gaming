package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "counter")
class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, name = "name")
    lateinit var name: String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Counter) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return Counter::class.hashCode()
    }
}