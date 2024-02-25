package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.LocationEntity
import org.springframework.data.repository.CrudRepository

interface LocationRepository : CrudRepository<LocationEntity, Long> {
    fun findByName(name: String): LocationEntity?
}