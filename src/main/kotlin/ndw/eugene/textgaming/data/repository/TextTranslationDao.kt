package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.TextTranslationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TextTranslationDao : JpaRepository<TextTranslationEntity, Long> {

    @Query(
        value = """
        SELECT DISTINCT tte.*
          FROM text_translations tte
          JOIN texts txt ON tte.text_id = txt.id
          JOIN conversations c ON c.text_id = txt.id
         WHERE c.location_id = :locationId
           AND tte.language_code = :langToFind
           AND NOT EXISTS (
             SELECT 1
               FROM text_translations tte2
              WHERE tte2.text_id = txt.id
                AND tte2.language_code = :langToExclude
           )
    """,
        nativeQuery = true
    )
    fun findConversationTextsExcludingLanguageNative(
        @Param("locationId") locationId: Long,
        @Param("langToFind") langToFind: String,
        @Param("langToExclude") langToExclude: String
    ): List<TextTranslationEntity>

    @Query(
        value = """
        SELECT DISTINCT tte.*
          FROM text_translations tte
          JOIN texts txt ON tte.text_id = txt.id
          JOIN options o ON o.text_id = txt.id
         WHERE o.location_id = :locationId
           AND tte.language_code = :langToFind
           AND NOT EXISTS (
             SELECT 1
               FROM text_translations tte2
              WHERE tte2.text_id = txt.id
                AND tte2.language_code = :langToExclude
           )
    """,
        nativeQuery = true
    )
    fun findOptionTextsExcludingLanguageNative(
        @Param("locationId") locationId: Long,
        @Param("langToFind") langToFind: String,
        @Param("langToExclude") langToExclude: String
    ): List<TextTranslationEntity>

    @Query(
        value = """
        SELECT DISTINCT tte.*
          FROM text_translations tte
          JOIN texts txt ON tte.text_id = txt.id
          JOIN characters ch ON ch.name_text_id = txt.id
          JOIN conversations c ON c.character_id = ch.id
         WHERE c.location_id = :locationId
           AND tte.language_code = :langToFind
           AND NOT EXISTS (
             SELECT 1
               FROM text_translations tte2
              WHERE tte2.text_id = txt.id
                AND tte2.language_code = :langToExclude
           )
    """,
        nativeQuery = true
    )
    fun findCharacterTextsExcludingLanguageNative(
        @Param("locationId") locationId: Long,
        @Param("langToFind") langToFind: String,
        @Param("langToExclude") langToExclude: String
    ): List<TextTranslationEntity>
}