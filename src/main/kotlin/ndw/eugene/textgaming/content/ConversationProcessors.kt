package ndw.eugene.textgaming.content

import ndw.eugene.textgaming.structure.services.ChoiceService
import ndw.eugene.textgaming.structure.data.LocationData
import ndw.eugene.textgaming.structure.data.GameState
import ndw.eugene.textgaming.structure.services.LocationService

typealias ConversationProcessor = (GameState) -> Unit
typealias OptionCondition = (GameState) -> Boolean

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
            choiceService.addChoice(it.gameId, Choice.FIORE_APPEARED)
        }
        processorsById["memorizeTonicBought"] = {
            choiceService.addChoice(it.gameId, Choice.BUY_TONIC)
        }
        processorsById["memorizeCompanionStory"] = {
            choiceService.addChoice(it.gameId, Choice.HEARD_COMPANION_STORY)
        }
        processorsById["memorizeStarsTalking"] = {
            choiceService.addChoice(it.gameId, Choice.STARS_TALKING)
        }
        processorsById["testProcessor"] = {
            choiceService.addChoice(it.gameId, Choice.TEST)
        }
        processorsById["memorizeOfferSolace"] = {
            choiceService.addChoice(it.gameId, Choice.OFFER_SOLACE)
        }
        processorsById["memorizeOfferTribute"] = {
            choiceService.addChoice(it.gameId, Choice.OFFER_TRIBUTE)
        }
        processorsById["memorizeOfferHope"] = {
            choiceService.addChoice(it.gameId, Choice.OFFER_HOPE)
        }
        processorsById["memorizeHeardAboutFather"] = {
            choiceService.addChoice(it.gameId, Choice.HEARD_ABOUT_FATHER)
        }
        processorsById["memorizeLostName"] = {
            choiceService.addChoice(it.gameId, Choice.LOST_NAME)
        }
        processorsById["memorizeTidyYourself"] = {
            choiceService.addChoice(it.gameId, Choice.TIDY_YOURSELF)
        }
        processorsById["memorizeHeardMerchantStory"] = {
            choiceService.addChoice(it.gameId, Choice.MERCHANT_STORY)
        }
        processorsById["memorizeSadSong"] = {
            choiceService.addChoice(it.gameId, Choice.SAD_SONG)
        }
        processorsById["memorizeFunnySong"] = {
            choiceService.addChoice(it.gameId, Choice.FUNNY_SONG)
        }
        processorsById["memorizeEpicSong"] = {
            choiceService.addChoice(it.gameId, Choice.EPIC_SONG)
        }
    }

    fun initOptionConditions() {
        optionsById["secondOptionChosen"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.TEST)
        }
        optionsById["retellCompanionStoryCheck"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.HEARD_COMPANION_STORY)
        }
        optionsById["checkStarsTalking"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.STARS_TALKING)
        }
        optionsById["checkStarsSilent"] = {
            val starsTalking = choiceService.checkChoiceHasBeenMade(it.gameId, Choice.STARS_TALKING)
            !starsTalking
        }
        optionsById["checkOutOfOffers"] = {
            val isOutOfOffers = choiceService.checkChoiceHasBeenMade(
                it.gameId,
                Choice.OFFER_SOLACE
            ) && choiceService.checkChoiceHasBeenMade(
                it.gameId,
                Choice.OFFER_TRIBUTE
            ) && choiceService.checkChoiceHasBeenMade(
                it.gameId,
                Choice.OFFER_HOPE
            )

            isOutOfOffers
        }
        optionsById["checkStillHaveOffers"] = {
            val isOutOfOffers = choiceService.checkChoiceHasBeenMade(
                it.gameId,
                Choice.OFFER_SOLACE
            ) && choiceService.checkChoiceHasBeenMade(
                it.gameId,
                Choice.OFFER_TRIBUTE
            ) && choiceService.checkChoiceHasBeenMade(
                it.gameId,
                Choice.OFFER_HOPE
            )

            !isOutOfOffers
        }
        optionsById["checkTonicWasBought"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.BUY_TONIC)
        }
        optionsById["checkDidntHearAboutFather"] = {
            !choiceService.checkChoiceHasBeenMade(it.gameId, Choice.HEARD_ABOUT_FATHER)
        }
        optionsById["checkHeardAboutFather"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.HEARD_ABOUT_FATHER)
        }
        optionsById["checkTidyYourself"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.TIDY_YOURSELF)
        }
        optionsById["checkNotTidyYourself"] = {
            !choiceService.checkChoiceHasBeenMade(it.gameId, Choice.TIDY_YOURSELF)
        }
        optionsById["checkLostName"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.LOST_NAME)
        }
        optionsById["checkHaveName"] = {
            !choiceService.checkChoiceHasBeenMade(it.gameId, Choice.LOST_NAME)
        }
        optionsById["checkHeardSadSong"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.SAD_SONG)
        }
        optionsById["checkHeardEpicSong"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.EPIC_SONG)
        }
        optionsById["checkHeardFunnySong"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.FUNNY_SONG)
        }
        optionsById["checkHeardMerchantStory"] = {
            choiceService.checkChoiceHasBeenMade(it.gameId, Choice.MERCHANT_STORY)
        }
        optionsById["checkHaveNoStoryToTell"] = {
            !choiceService.checkChoiceHasBeenMade(it.gameId, Choice.MERCHANT_STORY)
                    && !choiceService.checkChoiceHasBeenMade(it.gameId, Choice.SAD_SONG)
                    && !choiceService.checkChoiceHasBeenMade(it.gameId, Choice.EPIC_SONG)
                    && !choiceService.checkChoiceHasBeenMade(it.gameId, Choice.FUNNY_SONG)
                    && !choiceService.checkChoiceHasBeenMade(it.gameId, Choice.HEARD_COMPANION_STORY)
        }
    }

    fun getOptionConditionById(id: String): OptionCondition {
        return optionsById[id] ?: throw IllegalArgumentException("there is no option with id: $id")
    }

    fun getProcessorById(id: String): ConversationProcessor {
        return processorsById[id] ?: throw IllegalArgumentException("there is no processor with id: $id")
    }
}

private fun changeLocationTo(gameState: GameState, location: LocationData) {
    gameState.location = location.location
    gameState.currentConversationId = location.startId
}