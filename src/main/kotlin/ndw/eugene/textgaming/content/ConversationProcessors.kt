package ndw.eugene.textgaming.content

import ndw.eugene.textgaming.structure.services.ChoiceService
import ndw.eugene.textgaming.structure.data.LocationData
import ndw.eugene.textgaming.structure.data.UserState
import ndw.eugene.textgaming.structure.services.LocationService

typealias ConversationProcessor = (UserState) -> Unit
typealias OptionCondition = (UserState) -> Boolean

class ConversationProcessors {
    private val processorsById = mutableMapOf<String, ConversationProcessor>()
    private val optionsById = mutableMapOf<String, OptionCondition>()

    lateinit var locationService: LocationService
    lateinit var choiceService: ChoiceService

    fun initProcessors() {
        processorsById["changeLocationToAlleyways"] = {
            val location = locationService.getLocationData(Location.ALLEYWAYS)
            changeLocationTo(it, location)
            println("${it.location} : ${it.currentConversationId}")
        }
        processorsById["changeLocationToMarket"] = {
            val location = locationService.getLocationData(Location.MARKET)
            changeLocationTo(it, location)
            println("${it.location} : ${it.currentConversationId}")
        }
        processorsById["changeLocationToTower"] = {
            val location = locationService.getLocationData(Location.TOWER)
            changeLocationTo(it, location)
            println("${it.location} : ${it.currentConversationId}")
        }
        processorsById["changeLocationToDome"] = {
            val location = locationService.getLocationData(Location.DOME)
            changeLocationTo(it, location)
            println("${it.location} : ${it.currentConversationId}")
        }
        processorsById["memorizeFioreAppeared"] = {
            choiceService.addChoice(it.userId, Choice.FIORE_APPEARED)
        }
        processorsById["memorizeTonicBought"] = {
            choiceService.addChoice(it.userId, Choice.BUY_TONIC)
        }
        processorsById["memorizeCompanionStory"] = {
            choiceService.addChoice(it.userId, Choice.HEARD_COMPANION_STORY)
        }
        processorsById["memorizeStarsTalking"] = {
            choiceService.addChoice(it.userId, Choice.STARS_TALKING)
        }
        processorsById["testProcessor"] = {
            choiceService.addChoice(it.userId, Choice.TEST)
        }
        processorsById["memorizeOfferSolace"] = {
            choiceService.addChoice(it.userId, Choice.OFFER_SOLACE)
        }
        processorsById["memorizeOfferTribute"] = {
            choiceService.addChoice(it.userId, Choice.OFFER_TRIBUTE)
        }
        processorsById["memorizeOfferHope"] = {
            choiceService.addChoice(it.userId, Choice.OFFER_HOPE)
        }
    }

    fun initOptionConditions() {
        optionsById["marketChosen"] = {
            choiceService.checkChoiceHasBeenMade(it.userId, Choice.GO_TO_MARKET)
        }
        optionsById["alleywayChosen"] = {
            choiceService.checkChoiceHasBeenMade(it.userId, Choice.GO_TO_ALLEYWAYS)
        }
        optionsById["sadSongChosen"] = {
            choiceService.checkChoiceHasBeenMade(it.userId, Choice.SAD_SONG)
        }
        optionsById["funnySongChosen"] = {
            choiceService.checkChoiceHasBeenMade(it.userId, Choice.FUNNY_SONG)
        }
        optionsById["epicSongChosen"] = {
            choiceService.checkChoiceHasBeenMade(it.userId, Choice.EPIC_SONG)
        }
        optionsById["secondOptionChosen"] = {
            choiceService.checkChoiceHasBeenMade(it.userId, Choice.TEST)
        }
        optionsById["retellCompanionStoryCheck"] = {
            choiceService.checkChoiceHasBeenMade(it.userId, Choice.HEARD_COMPANION_STORY)
        }
        optionsById["checkStartsTalking"] = {
            choiceService.checkChoiceHasBeenMade(it.userId, Choice.STARS_TALKING)
        }
        optionsById["checkStartsSilent"] = {
            val starsTalking = choiceService.checkChoiceHasBeenMade(it.userId, Choice.STARS_TALKING)
            !starsTalking
        }
        optionsById["checkOutOfOffers"] = {
            val isOutOfOffers = choiceService.checkChoiceHasBeenMade(
                it.userId,
                Choice.OFFER_SOLACE
            ) && choiceService.checkChoiceHasBeenMade(
                it.userId,
                Choice.OFFER_TRIBUTE
            ) && choiceService.checkChoiceHasBeenMade(
                it.userId,
                Choice.OFFER_HOPE
            )

            isOutOfOffers
        }
        optionsById["checkStillHaveOffers"] = {
            val isOutOfOffers = choiceService.checkChoiceHasBeenMade(
                it.userId,
                Choice.OFFER_SOLACE
            ) && choiceService.checkChoiceHasBeenMade(
                it.userId,
                Choice.OFFER_TRIBUTE
            ) && choiceService.checkChoiceHasBeenMade(
                it.userId,
                Choice.OFFER_HOPE
            )

            !isOutOfOffers
        }
        optionsById["checkTonicWasBought"] = {
            choiceService.checkChoiceHasBeenMade(it.userId, Choice.BUY_TONIC)
        }
    }

    fun getOptionConditionById(id: String): OptionCondition {
        return optionsById[id] ?: throw IllegalArgumentException("there is no option with id: $id")
    }

    fun getProcessorById(id: String): ConversationProcessor {
        return processorsById[id] ?: throw IllegalArgumentException("there is no processor with id: $id")
    }
}

private fun changeLocationTo(user: UserState, location: LocationData) {
    user.location = location.location
    user.currentConversationId = location.startId
}