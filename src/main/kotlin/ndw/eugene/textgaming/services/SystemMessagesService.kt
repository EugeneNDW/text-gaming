package ndw.eugene.textgaming.services

import ndw.eugene.textgaming.data.entity.TextEntity

class SystemMessagesService private constructor() {
    companion object {
        private const val DEFAULT_MESSAGE = "Message not found."

        private val enMessages: Map<SystemMessageType, String> = mapOf(
            SystemMessageType.DEFAULT_OPTION_BUTTON_TEXT to "continue...",
            SystemMessageType.NEW_GAME_BUTTON_TEXT to "Embark on another ludicrous journey",
            SystemMessageType.REPORT_RECEIVED_MESSAGE to "The Admiralty has received your report. It will be considered.",
            SystemMessageType.FEEDBACK_RECEIVED_MESSAGE to "Thank you for your feedback! The Admiralty will treat it with the utmost attention.",
            SystemMessageType.GAME_STARTED_MESSAGE to "Your journey has already begun. If you restart, the progress will be lost.",
            SystemMessageType.WELCOME_MESSAGE to "Welcome to the World. Don’t get lost. Forget who you are.",
            SystemMessageType.COPYRIGHT_MESSAGE to """
    <b>Copyright © 2024 Sofia Ezdina. All Rights Reserved.</b>
Welcome to The Navigator! This game and all its parts (like stories, characters, and graphics) are created by me, Sofia Ezdina, and are protected by law. You're welcome to play this game for your own fun, but please don't copy, share, or sell any part of it without my permission.
Enjoy the adventure, and if you need to reach out, contact me at godhasleft@gmail.com
"""
        )

        private val ruMessages: Map<SystemMessageType, String> = mapOf(
            SystemMessageType.DEFAULT_OPTION_BUTTON_TEXT to "Продолжить...",
            SystemMessageType.NEW_GAME_BUTTON_TEXT to "Отправиться в очередное курьёзное путешествие",
            SystemMessageType.REPORT_RECEIVED_MESSAGE to "Адмиралтейство получило Ваш отчёт. Мы его рассмотрим.",
            SystemMessageType.FEEDBACK_RECEIVED_MESSAGE to "Спасибо за Ваш отзыв! Адмиралтейство отнесется к нему с тщательнейшим вниманием.",
            SystemMessageType.GAME_STARTED_MESSAGE to "Ваше путешествие уже началось. При перезапуске текущий прогресс будет утерян.",
            SystemMessageType.WELCOME_MESSAGE to "Добро пожаловать в Мир. Не пропадите. Забудьте себя.",
        )

        private val storage: Map<String, Map<SystemMessageType, String>> = mapOf(
            Locale.EN.name to enMessages,
            Locale.RU.name to ruMessages
        )

        fun getMessage(locale: String, type: SystemMessageType): String {
            val messages = storage[locale] ?: enMessages
            return messages[type] ?: enMessages[type] ?: DEFAULT_MESSAGE
        }

        fun getLocalizedText(text: TextEntity?, languageCode: String): String {
            if (text == null) return ""
            val translation = text.translations.find { it.id?.languageCode == languageCode }
            return translation?.translatedText ?: fallbackText(text)
        }

        private fun fallbackText(text: TextEntity): String {
            val fallback = text.translations.find { it.id?.languageCode == "en" }
            return fallback?.translatedText ?: ""
        }
    }
}


enum class Locale {
    RU, EN
}

enum class SystemMessageType {
    DEFAULT_OPTION_BUTTON_TEXT, NEW_GAME_BUTTON_TEXT, REPORT_RECEIVED_MESSAGE, FEEDBACK_RECEIVED_MESSAGE, GAME_STARTED_MESSAGE, WELCOME_MESSAGE, COPYRIGHT_MESSAGE
}