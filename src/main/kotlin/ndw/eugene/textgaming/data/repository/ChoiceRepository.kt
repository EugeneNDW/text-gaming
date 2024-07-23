package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.Choice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChoiceRepository : JpaRepository<Choice, Long> {
    fun findByName(name: String): Choice?
}