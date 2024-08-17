package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.ConversationEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface ConversationRepository : CrudRepository<ConversationEntity, Long> {
    @Query(
        value = "SELECT c.* FROM conversations c INNER JOIN locations l ON c.location_id = l.id WHERE l.name = :locationName AND c.id = :conversationId",
        nativeQuery = true
    )
    fun getByLocationAndConversationId(
        @Param("locationName") locationName: String,
        @Param("conversationId") conversationId: Long
    ): ConversationEntity
}