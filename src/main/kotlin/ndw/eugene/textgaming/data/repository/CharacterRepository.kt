package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.CharacterEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CharacterRepository : JpaRepository<CharacterEntity, Long> {

    @Query("""
        SELECT c
        FROM CharacterEntity c
        JOIN c.nameText t
        JOIN t.translations tr
        WHERE tr.translatedText = :translatedName
          AND tr.id.languageCode = :languageCode
    """)
    fun findByName(
        @Param("translatedName") translatedName: String,
        @Param("languageCode") languageCode: String = "en"
    ): CharacterEntity?
}
