package ndw.eugene.textgaming.services

import ndw.eugene.textgaming.DEFAULT_OPTION_TEXT
import ndw.eugene.textgaming.data.entity.TextTranslationEntity
import ndw.eugene.textgaming.data.entity.TextTranslationKey
import ndw.eugene.textgaming.data.repository.TextDao
import ndw.eugene.textgaming.data.repository.TextTranslationDao
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TextTranslationService(private val textRepository: TextDao, private val textTranslationDao: TextTranslationDao) {
    fun addNewTranslationForText(
        textId: Long,
        languageCode: String,
        translatedText: String
    ): TextTranslationEntity {
        // 1) Retrieve the existing TextEntity
        val textEntity = textRepository.findById(textId)
            .orElseThrow { IllegalArgumentException("TextEntity with id=$textId not found.") }

        // 2) Create a new TextTranslationEntity for the new language
        val translationKey = TextTranslationKey(
            textId = textId,
            languageCode = languageCode.lowercase()
        )
        val newTranslation = TextTranslationEntity().apply {
            id = translationKey
            this.text = textEntity
            this.translatedText = translatedText
        }

        // 3) Save the new translation
        return textTranslationDao.save(newTranslation)
    }

    fun getTranslationsForLocation(
        locationId: Long,
        langToFind: String,
        langToExclude: String
    ): List<TextTranslationEntity> {
        val conversationTexts = textTranslationDao.findConversationTextsExcludingLanguageNative(locationId, langToFind, langToExclude)
        val optionTexts = textTranslationDao.findOptionTextsExcludingLanguageNative(locationId, langToFind, langToExclude)
        val characterTexts = textTranslationDao.findCharacterTextsExcludingLanguageNative(locationId, langToFind, langToExclude)

        val combined = mutableListOf<TextTranslationEntity>()
        combined.addAll(conversationTexts)
        combined.addAll(optionTexts)
        combined.addAll(characterTexts)

        return combined.filter { it.translatedText != "..." }.sortedBy { it.text?.id }
    }

}