package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.TextEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TextDao : JpaRepository<TextEntity, Long> {
}