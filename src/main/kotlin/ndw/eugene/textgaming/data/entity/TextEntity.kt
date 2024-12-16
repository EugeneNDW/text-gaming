package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "texts")
class TextEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToMany(mappedBy = "text", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var translations: MutableList<TextTranslationEntity> = mutableListOf()

    fun addTranslation(translation: TextTranslationEntity) {
        translations.add(translation)
        translation.text = this
    }
}
