package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.LocationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface LocationRepository : JpaRepository<LocationEntity, Long> {
    fun findByName(name: String): LocationEntity?
}