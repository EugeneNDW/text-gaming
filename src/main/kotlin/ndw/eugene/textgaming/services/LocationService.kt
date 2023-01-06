package ndw.eugene.textgaming.services

import mu.KotlinLogging
import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.data.LocationData
import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.data.repository.GameStateRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class LocationService(
    private val gameStateRepository: GameStateRepository
) {
    private var locationsByName: Map<Location, LocationData> = mutableMapOf()

    fun initLocationService(locationsByName: Map<Location, LocationData>) {
        this.locationsByName = locationsByName
    }

    fun changeLocationTo(gameState: GameState, location: Location) {
        val locationData = getLocationData(location)

        gameState.location = locationData.location
        gameState.currentConversationId = locationData.startId

        gameStateRepository.save(gameState)

        logger.info { "location for game with id ${gameState.id} was changed to $location" }
    }

    fun getLocationData(location: Location): LocationData {
        return locationsByName[location] ?: throw IllegalArgumentException()
    }
}