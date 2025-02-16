package ndw.eugene.textgaming.controllers

import ndw.eugene.textgaming.services.TextTranslationService
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["http://localhost:3000"])
@RequestMapping("/games/1")
class TextController(private val textTranslationService: TextTranslationService) {
    @GetMapping("/locations/{locationId}/texts")
    fun getAllTextsFromLocations(
        @PathVariable("locationId") locationId: Long,
        @RequestParam("langToFind") langToFind: String,
        @RequestParam("langToExclude") langToExclude: String
        ): List<TranslationDto> {

        val translationsForLocation =
            textTranslationService.getTranslationsForLocation(locationId, langToFind.lowercase(), langToExclude.lowercase())

        return translationsForLocation.map {
            TranslationDto(
                it.text?.id!!, it.translatedText
            )
        }
    }

    @PostMapping("/locations/{locationId}/texts/{textId}")
    fun saveNewTranslation(
        @PathVariable("textId") textId: Long,
        @RequestBody request: CreateTranslationRequestDto
    ) {
        textTranslationService.addNewTranslationForText(textId, request.languageCode, request.translatedText)
    }
}

data class CreateTranslationRequestDto(val languageCode: String, val translatedText: String) {
}

data class TranslationDto(val textId: Long, val translatedText: String) {
}