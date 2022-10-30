package ndw.eugene.textgaming.structure.services

import ndw.eugene.textgaming.content.Choice

class ChoiceService {
    private val userToChoices: MutableMap<Long, MutableSet<Choice>> = mutableMapOf()

    fun addChoice(userId: Long, choice: Choice) {
        val usersChoices = getUserChoices(userId)

        usersChoices.add(choice)
    }

    fun checkChoiceHasBeenMade(userId: Long, choice: Choice): Boolean {
        val usersChoices = getUserChoices(userId)

        return usersChoices.contains(choice)
    }

    private fun getUserChoices(userId: Long): MutableSet<Choice> {
        val choices = userToChoices.getOrPut(userId) {
            mutableSetOf()
        }

        return choices
    }
}