package ndw.eugene.textgaming.content

import ndw.eugene.textgaming.structure.data.entity.GameState
import ndw.eugene.textgaming.structure.services.ChoiceService
import ndw.eugene.textgaming.structure.services.LocationService
import org.springframework.stereotype.Component

typealias ConversationProcessor = (GameState) -> Unit
typealias OptionCondition = (GameState) -> Boolean

@Component
class ConversationProcessors(
    private val choiceService: ChoiceService,
    private val locationService: LocationService
) {
    private val processorsById = mutableMapOf<String, ConversationProcessor>()
    private val optionsById = mutableMapOf<String, OptionCondition>()

    init {
        initProcessors()
        initOptionConditions()
    }

    fun getOptionConditionById(id: String): OptionCondition {
        return optionsById[id] ?: throw IllegalArgumentException("there is no option with id: $id")
    }

    fun getProcessorById(id: String): ConversationProcessor {
        return processorsById[id] ?: throw IllegalArgumentException("there is no processor with id: $id")
    }

    private fun initProcessors() {
        processorsById["changeLocationToAlleyways"] = {
            locationService.changeLocationTo(it, Location.ALLEYWAYS)
        }
        processorsById["changeLocationToMarket"] = {
            locationService.changeLocationTo(it, Location.MARKET)
        }
        processorsById["changeLocationToTower"] = {
            locationService.changeLocationTo(it, Location.TOWER)
        }
        processorsById["changeLocationToDome"] = {
            locationService.changeLocationTo(it, Location.DOME)
        }
        processorsById["memorizeFioreAppeared"] = {
            choiceService.addChoice(it, Choice.FIORE_APPEARED)
        }
        processorsById["memorizeTonicBought"] = {
            choiceService.addChoice(it, Choice.BUY_TONIC)
        }
        processorsById["memorizeCompanionStory"] = {
            choiceService.addChoice(it, Choice.HEARD_COMPANION_STORY)
        }
        processorsById["memorizeStarsTalking"] = {
            choiceService.addChoice(it, Choice.STARS_TALKING)
        }
        processorsById["testProcessor"] = {
            choiceService.addChoice(it, Choice.TEST)
        }
        processorsById["memorizeOfferSolace"] = {
            choiceService.addChoice(it, Choice.OFFER_SOLACE)
        }
        processorsById["memorizeOfferTribute"] = {
            choiceService.addChoice(it, Choice.OFFER_TRIBUTE)
        }
        processorsById["memorizeOfferHope"] = {
            choiceService.addChoice(it, Choice.OFFER_HOPE)
        }
        processorsById["memorizeHeardAboutFather"] = {
            choiceService.addChoice(it, Choice.HEARD_ABOUT_FATHER)
        }
        processorsById["memorizeLostName"] = {
            choiceService.addChoice(it, Choice.LOST_NAME)
        }
        processorsById["memorizeTidyYourself"] = {
            choiceService.addChoice(it, Choice.TIDY_YOURSELF)
        }
        processorsById["memorizeHeardMerchantStory"] = {
            choiceService.addChoice(it, Choice.MERCHANT_STORY)
        }
        processorsById["memorizeSadSong"] = {
            choiceService.addChoice(it, Choice.SAD_SONG)
        }
        processorsById["memorizeFunnySong"] = {
            choiceService.addChoice(it, Choice.FUNNY_SONG)
        }
        processorsById["memorizeEpicSong"] = {
            choiceService.addChoice(it, Choice.EPIC_SONG)
        }
    }

    private fun initOptionConditions() {
        optionsById["testCondition"] = {
            true
        }

        optionsById["testNotCondition"] = {
            false
        }

        optionsById["secondOptionChosen"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.TEST)
        }
        optionsById["retellCompanionStoryCheck"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.HEARD_COMPANION_STORY)
        }
        optionsById["checkStarsTalking"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.STARS_TALKING)
        }
        optionsById["checkStarsSilent"] = {
            val starsTalking = choiceService.checkChoiceHasBeenMade(it, Choice.STARS_TALKING)
            !starsTalking
        }
        optionsById["checkOutOfOffers"] = {
            val isOutOfOffers = choiceService.checkChoiceHasBeenMade(
                it,
                Choice.OFFER_SOLACE
            ) && choiceService.checkChoiceHasBeenMade(
                it,
                Choice.OFFER_TRIBUTE
            ) && choiceService.checkChoiceHasBeenMade(
                it,
                Choice.OFFER_HOPE
            )

            isOutOfOffers
        }
        optionsById["checkStillHaveOffers"] = {
            val isOutOfOffers = choiceService.checkChoiceHasBeenMade(
                it,
                Choice.OFFER_SOLACE
            ) && choiceService.checkChoiceHasBeenMade(
                it,
                Choice.OFFER_TRIBUTE
            ) && choiceService.checkChoiceHasBeenMade(
                it,
                Choice.OFFER_HOPE
            )

            !isOutOfOffers
        }
        optionsById["checkTonicWasBought"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.BUY_TONIC)
        }
        optionsById["checkDidntHearAboutFather"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.HEARD_ABOUT_FATHER)
        }
        optionsById["checkHeardAboutFather"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.HEARD_ABOUT_FATHER)
        }
        optionsById["checkTidyYourself"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.TIDY_YOURSELF)
        }
        optionsById["checkNotTidyYourself"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.TIDY_YOURSELF)
        }
        optionsById["checkLostName"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.LOST_NAME)
        }
        optionsById["checkHaveName"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.LOST_NAME)
        }
        optionsById["checkHeardSadSong"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.SAD_SONG)
        }
        optionsById["checkHeardEpicSong"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.EPIC_SONG)
        }
        optionsById["checkHeardFunnySong"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.FUNNY_SONG)
        }
        optionsById["checkHeardMerchantStory"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.MERCHANT_STORY)
        }
        optionsById["checkHaveNoStoryToTell"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.MERCHANT_STORY)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.SAD_SONG)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.EPIC_SONG)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.FUNNY_SONG)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.HEARD_COMPANION_STORY)
        }
    }
}