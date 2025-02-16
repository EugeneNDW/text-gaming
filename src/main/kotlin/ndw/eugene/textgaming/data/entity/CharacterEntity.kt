package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "characters")
class CharacterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = [CascadeType.ALL])
    @JoinColumn(name = "name_text_id", nullable = false)
    var nameText: TextEntity? = null

}
