package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.Counter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CounterRepository : JpaRepository<Counter, Long> {
    fun findByName(name: String): Counter?
}
