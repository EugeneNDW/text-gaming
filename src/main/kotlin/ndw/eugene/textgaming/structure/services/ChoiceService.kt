package ndw.eugene.textgaming.structure.services

import ndw.eugene.textgaming.content.Choice

class ChoiceService {
    private val gameToChoices: MutableMap<Long, MutableSet<Choice>> = mutableMapOf()

    fun addChoice(gameId: Long, choice: Choice) {
        val usersChoices = getChoicesForGame(gameId)

        usersChoices.add(choice)
    }

    fun checkChoiceHasBeenMade(gameId: Long, choice: Choice): Boolean {
        val usersChoices = getChoicesForGame(gameId)

        return usersChoices.contains(choice)
    }

    private fun getChoicesForGame(gameId: Long): MutableSet<Choice> {
        val choices = gameToChoices.getOrPut(gameId) {
            mutableSetOf()
        }

        return choices
    }
}