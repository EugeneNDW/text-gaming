package ndw.eugene.textgaming.controllers

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ndw.eugene.textgaming.loaders.Conversation
import ndw.eugene.textgaming.loaders.ConversationLoader
import ndw.eugene.textgaming.services.LocationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/upload")
class UploadController(
    private val conversationLoader: ConversationLoader,
    private val locationService: LocationService,
) {
    @PostMapping(consumes = ["multipart/form-data"])
    fun uploadLocation(@RequestPart("files") files: List<MultipartFile>) {
        files
            .map { file ->
                val content = file.inputStream.bufferedReader().use { it.readText() }
                Json.decodeFromString<Conversation>(content)
            }.map {
                if (locationService.findByName(it.locationName) == null) {
                    conversationLoader.loadLocation(it)
                }
            }
    }
}