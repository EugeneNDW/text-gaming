package ndw.eugene.textgaming.data.entity

import jakarta.persistence.*


@Entity
@Table(name = "text_translations")
class TextTranslationEntity {

    @EmbeddedId
    var id: TextTranslationKey? = null

    @ManyToOne
    @MapsId("textId")
    @JoinColumn(name = "text_id")
    var text: TextEntity? = null

    @Column(name = "translated_text", nullable = false)
    var translatedText: String = ""
}

@Embeddable
class TextTranslationKey(
    @Column(name = "text_id")
    var textId: Long = 0,

    @Column(name = "language_code")
    var languageCode: String = ""
) : java.io.Serializable
