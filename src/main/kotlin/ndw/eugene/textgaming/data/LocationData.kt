package ndw.eugene.textgaming.data

import ndw.eugene.textgaming.content.Location

class LocationData(
    val location: Location,
    val startId: Long,
    val convById: Map<Long, ConversationPart>,
    val convToOption: Map<Long, MutableList<Option>>
)