package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.TextTranslationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TextTranslationDao : JpaRepository<TextTranslationEntity, Long> {

    @Query(
        """
        SELECT DISTINCT tt
        FROM TextTranslationEntity tt
        JOIN tt.text t
        LEFT JOIN ConversationEntity c ON c.text = t
        LEFT JOIN OptionEntity o ON o.text = t
        WHERE tt.id.languageCode = :langToFind
          AND (c.locationId = :locationId OR o.locationId = :locationId)
          AND NOT EXISTS (
              SELECT 1 FROM TextTranslationEntity tte2
              WHERE tte2.text = t
                AND tte2.id.languageCode = :langToExclude
          )
    """
    )
    fun findAllByLocationAndLangExcludingAnother(
        @Param("locationId") locationId: Long,
        @Param("langToFind") langToFind: String,
        @Param("langToExclude") langToExclude: String
    ): List<TextTranslationEntity>


}