package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.ConversationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ConversationRepository : JpaRepository<ConversationEntity, Long> {
    @Query(
        value = "SELECT c.* FROM conversations c INNER JOIN locations l ON c.location_id = l.id WHERE l.name = :locationName AND c.id = :conversationId",
        nativeQuery = true
    )
    fun getByLocationAndConversationId(
        @Param("locationName") locationName: String,
        @Param("conversationId") conversationId: Long
    ): ConversationEntity

    fun findByLocationId(locationId: Long): List<ConversationEntity>
}