package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.CharacterEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CharacterRepository : JpaRepository<CharacterEntity, Long> {
    fun findByName(name: String): CharacterEntity?
}
