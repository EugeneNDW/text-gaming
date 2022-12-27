package ndw.eugene.textgaming.structure.services

import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.structure.data.GameState
import ndw.eugene.textgaming.structure.data.LocationData
import org.springframework.stereotype.Service

@Service
class LocationService {
    private var locationsByName: Map<Location, LocationData> = mutableMapOf()

    fun initLocationService(locationsByName: Map<Location, LocationData>) {
        this.locationsByName = locationsByName
    }

    fun changeLocationTo(gameState: GameState, location: Location) {
        val locationData = getLocationData(location)
        gameState.location = locationData.location
        gameState.currentConversationId = locationData.startId
        println("${gameState.location} : ${gameState.currentConversationId}")
    }

    fun getLocationData(location: Location): LocationData {
        return locationsByName[location] ?: throw IllegalArgumentException()
    }
}