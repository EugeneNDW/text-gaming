package ndw.eugene.textgaming.services

import mu.KotlinLogging
import ndw.eugene.textgaming.data.entity.Choice
import ndw.eugene.textgaming.data.entity.GameChoice
import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.data.repository.ChoiceRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ChoiceService(
    private val choiceRepository: ChoiceRepository
) {

    fun getAllChoices(): MutableList<Choice> {
        return choiceRepository.findAll()
    }

    fun createChoiceIfNotExists(choiceName: String) {
        val choiceByName = choiceRepository.findByName(choiceName)
        if (choiceByName != null) {
            return
        }

        val choice = Choice()
        choice.name = choiceName
        choiceRepository.save(choice)
    }

    fun addChoice(gameState: GameState, choice: String) {
        val gameChoice = GameChoice()
        gameChoice.choice = choiceRepository.findByName(choice) ?: throw IllegalArgumentException("can't find choice")
        gameState.addChoice(gameChoice)
        logger.info { "user: ${gameState.userId} made choice: $choice in game: ${gameState.id}" }
    }
}