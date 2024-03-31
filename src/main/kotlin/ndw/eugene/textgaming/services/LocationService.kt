package ndw.eugene.textgaming.services

import mu.KotlinLogging
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.data.repository.GameStateRepository
import ndw.eugene.textgaming.data.repository.LocationRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class LocationService(
    private val gameStateRepository: GameStateRepository,
    private val locationRepository: LocationRepository
) {
    fun changeLocationTo(gameState: GameState, location: String) {
        val locationEntity = locationRepository.findByName(location) ?: throw IllegalArgumentException()
        gameState.location = Location.valueOf(locationEntity.name)
        gameState.currentConversationId = locationEntity.startId
        gameStateRepository.save(gameState)
        logger.info { "location for game with id ${gameState.id} was changed to $location" }
    }
}