package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "characters")
class CharacterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    var name: String = ""
}
