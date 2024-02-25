package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.OptionEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface OptionRepository : CrudRepository<OptionEntity, UUID> {

    @Query(value = "SELECT o.* FROM options o INNER JOIN locations l ON o.location_id = l.id WHERE l.name = :locationName AND o.from_id = :fromId", nativeQuery = true)
    fun findAllByFromIdAndLocation(@Param("fromId") fromId: Long, @Param("locationName") locationName: String): List<OptionEntity>
}