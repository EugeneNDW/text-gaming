package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.OptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface OptionRepository : JpaRepository<OptionEntity, UUID> {

    @Query(
        value = "SELECT o.* FROM options o INNER JOIN locations l ON o.location_id = l.id WHERE l.name = :locationName AND o.from_id = :fromId",
        nativeQuery = true
    )
    fun findAllByFromIdAndLocation(
        @Param("fromId") fromId: Long,
        @Param("locationName") locationName: String
    ): List<OptionEntity>

    fun findByLocationId(locationId: Long): List<OptionEntity>

    fun findByFromId(fromId: Long): List<OptionEntity>
}