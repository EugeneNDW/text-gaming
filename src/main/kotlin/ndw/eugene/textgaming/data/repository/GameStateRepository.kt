package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.GameState
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface GameStateRepository : CrudRepository<GameState, Long> {

    @Query(
        "SELECT gs FROM GameState gs " +
                "WHERE gs.id = (SELECT MAX(sgs.id) FROM GameState sgs WHERE sgs.userId = :userId)"
    )
    fun findGameStateWithMaxIdByUserId(userId: Long): GameState?
}