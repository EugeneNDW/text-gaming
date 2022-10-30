package ndw.eugene.textgaming.structure.services

import ndw.eugene.textgaming.content.Location
import ndw.eugene.textgaming.structure.data.LocationData
import ndw.eugene.textgaming.structure.data.Option
import ndw.eugene.textgaming.utils.ConversationLoader

class LocationService(
    private val loader: ConversationLoader,
) {
    var locationsByName: MutableMap<Location, LocationData> = mutableMapOf()

    fun initLocations() {
        locationsByName = loader.loadLocations()
    }

    fun getOptionsByLocationAndConversationId(location: Location, conversationId: Long): List<Option> {
        val locationData = getLocationData(location)
        return locationData.convToOption[conversationId] ?: listOf()
    }

    fun getLocationData(location: Location): LocationData {
        return locationsByName[location] ?: throw IllegalArgumentException()
    }
}