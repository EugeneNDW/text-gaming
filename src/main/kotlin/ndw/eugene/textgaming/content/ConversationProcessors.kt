package ndw.eugene.textgaming.content

import ndw.eugene.textgaming.data.entity.CounterType
import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.services.ChoiceService
import ndw.eugene.textgaming.services.CounterService
import ndw.eugene.textgaming.services.LocationService
import org.springframework.stereotype.Component

typealias ConversationProcessor = (GameState) -> Unit
typealias OptionCondition = (GameState) -> Boolean

private const val BAD_GUY_POINTS_REQUIRED = 5
private const val SHEPHERD_IS_FRIEND_MIN_POINTS = 3

@Component
class ConversationProcessors(
    private val choiceService: ChoiceService,
    private val locationService: LocationService,
    private val counterService: CounterService,
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
        processorsById["changeLocationToShip"] = {
            locationService.changeLocationTo(it, Location.SHIP)
        }
        processorsById["changeLocationToStorm"] = {
            locationService.changeLocationTo(it, Location.STORM)
        }
        processorsById["changeLocationToSunkenLibrary"] = {
            locationService.changeLocationTo(it, Location.SUNKEN_LIBRARY_SHORE)
        }
        processorsById["changeLocationToJungle"] = {
            locationService.changeLocationTo(it, Location.JUNGLE)
        }
        processorsById["changeLocationToHarpyEncounter"] = {
            locationService.changeLocationTo(it, Location.HARPY_ENCOUNTER)
        }
        processorsById["changeLocationToShepherdEncounter"] = {
            locationService.changeLocationTo(it, Location.SHEPHERD_ENCOUNTER)
        }
        processorsById["changeLocationToSunkenLibraryInside"] = {
            locationService.changeLocationTo(it, Location.SUNKEN_LIBRARY_INSIDE)
        }
        processorsById["changeLocationToHarpiesLair"] = {
            locationService.changeLocationTo(it, Location.HARPIES_LAIR)
        }
        processorsById["changeLocationToBoyEpilogue"] = {
            locationService.changeLocationTo(it, Location.BOY_EPILOGUE)
        }
        processorsById["changeLocationToWizardEpilogue"] = {
            locationService.changeLocationTo(it, Location.WIZARD_EPILOGUE)
        }
        processorsById["changeLocationToTowerEpilogue"] = {
            locationService.changeLocationTo(it, Location.TOWER_EPILOGUE)
        }
        processorsById["changeLocationToShipEpilogue"] = {
            locationService.changeLocationTo(it, Location.SHIP_EPILOGUE)
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
        processorsById["memorizeResearch"] = {
            choiceService.addChoice(it, Choice.RESEARCH)
        }
        processorsById["memorizeWhatHappenedToTheLibrary"] = {
            choiceService.addChoice(it, Choice.WHAT_HAPPENED_TO_THE_LIBRARY)
        }
        processorsById["memorizeDestroyed"] = {
            choiceService.addChoice(it, Choice.ASKED_DESTROYED_HOW)
        }
        processorsById["memorizeLibrarians"] = {
            choiceService.addChoice(it, Choice.ASKED_LIBRARIANS)
        }
        processorsById["memorizeSpyglass"] = {
            choiceService.addChoice(it, Choice.SPYGLASS)
        }
        processorsById["memorizeTookTablet"] = {
            choiceService.addChoice(it, Choice.TOOK_TABLET)
        }
        processorsById["memorizeExaminedStatue"] = {
            choiceService.addChoice(it, Choice.EXAMINED_STATUE)
        }
        processorsById["memorizeExaminedShelves"] = {
            choiceService.addChoice(it, Choice.EXAMINED_SHELVES)
        }
        processorsById["memorizePickStar"] = {
            choiceService.addChoice(it, Choice.PICK_STAR)
        }
        processorsById["memorizeWasInHarpyEncounter"] = {
            choiceService.addChoice(it, Choice.WAS_IN_HARPY_ENCOUNTER)
        }
        processorsById["memorizeWasInShepherdEncounter"] = {
            choiceService.addChoice(it, Choice.WAS_IN_SHEPHERD_ENCOUNTER)
        }
        processorsById["memorizeLiedAboutPlants"] = {
            choiceService.addChoice(it, Choice.LIE_ABOUT_PLANTS)
        }
        processorsById["memorizeLiedAboutRocks"] = {
            choiceService.addChoice(it, Choice.LIE_ABOUT_ROCKS)
        }
        processorsById["memorizeLiedAboutBeetles"] = {
            choiceService.addChoice(it, Choice.LIE_ABOUT_BEETLES)
        }
        processorsById["memorizeLeftShepherdToLibrary"] = {
            choiceService.addChoice(it, Choice.LEFT_SHEPHERD_TO_LIBRARY)
        }
        processorsById["memorizeLeftShepherdToHarpies"] = {
            choiceService.addChoice(it, Choice.LEFT_SHEPHERD_TO_HARPIES)
        }
        processorsById["memorizeLeftShepherdToJungle"] = {
            choiceService.addChoice(it, Choice.LEFT_SHEPHERD_TO_JUNGLE)
        }
        processorsById["memorizeGaveShepherdTonic"] = {
            choiceService.addChoice(it, Choice.GIVE_TONIC_TO_SHEPHERD)
        }
        processorsById["memorizeTipMusicians"] = {
            choiceService.addChoice(it, Choice.TIP_MUSICIANS)
        }
        processorsById["memorizeGotWizard"] = {
            choiceService.addChoice(it, Choice.GOT_THE_WIZARD)
        }
        processorsById["memorizeGotBoy"] = {
            choiceService.addChoice(it, Choice.GOT_THE_BOY)
        }
        processorsById["memorizeGotGirl"] = {
            choiceService.addChoice(it, Choice.GOT_THE_GIRL)
        }
        processorsById["memorizeCanEarnMoney"] = {
            choiceService.addChoice(it, Choice.CAN_EARN_MONEY)
        }

        processorsById["increaseBoyRelationshipCounter"] = {
            counterService.increaseCounter(it, CounterType.BOY_RELATIONSHIP)
        }
        processorsById["decreaseBoyRelationshipCounter"] = {
            counterService.decreaseCounter(it, CounterType.BOY_RELATIONSHIP)
        }
        processorsById["increaseBadGuyCounter"] = {
            counterService.increaseCounter(it, CounterType.BAD_GUY)
        }
        processorsById["decreaseBadGuyCounter"] = {
            counterService.decreaseCounter(it, CounterType.BAD_GUY)
        }

        processorsById["endGame"] = {
            //todo сделать механизм завершения игры
        }
    }

    private fun initOptionConditions() {
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
        optionsById["checkDidntHearEpicSong"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.EPIC_SONG)
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
        optionsById["checkAskedAboutResearch"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.RESEARCH)
        }
        optionsById["checkKnowsAboutLibrary"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.WHAT_HAPPENED_TO_THE_LIBRARY)
        }
        optionsById["checkDontKnowAboutLibrary"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.WHAT_HAPPENED_TO_THE_LIBRARY)
        }
        optionsById["checkDestroyed"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.ASKED_DESTROYED_HOW)
        }
        optionsById["checkNotDestroyed"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.ASKED_DESTROYED_HOW)
        }
        optionsById["checkLibrarians"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.ASKED_LIBRARIANS)
        }
        optionsById["checkNotLibrarians"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.ASKED_LIBRARIANS)
        }
        optionsById["checkMetTheHarpy"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.MET_THE_HARPY)
        }
        optionsById["checkDidntMeetTheHarpy"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.MET_THE_HARPY)
        }
        optionsById["checkWentThroughStorm"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.WENT_THROUGH_THE_STORM)
        }
        optionsById["checkDidntGoThroughStorm"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.WENT_THROUGH_THE_STORM)
        }
        optionsById["checkStatuesNotExamined"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.EXAMINED_STATUE)
        }
        optionsById["checkStatueExamined"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.EXAMINED_STATUE)
        }
        optionsById["checkShelvesNotExamined"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.EXAMINED_SHELVES)
        }
        optionsById["checkShelvesExamined"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.EXAMINED_SHELVES)
        }
        optionsById["checkTabletIsNotTaken"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.TOOK_TABLET)
        }
        optionsById["checkTabletIsTaken"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.TOOK_TABLET)
        }
        optionsById["checkLeftShepherdToLibrary"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.LEFT_SHEPHERD_TO_LIBRARY)
        }
        optionsById["checkLeftShepherdToHarpies"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.LEFT_SHEPHERD_TO_HARPIES)
        }
        optionsById["checkLeftShepherdToJungle"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.LEFT_SHEPHERD_TO_JUNGLE)
        }
        optionsById["checkGaveShepherdTonic"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.GIVE_TONIC_TO_SHEPHERD)
        }
        optionsById["checkSpyglass"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.SPYGLASS)
        }
        optionsById["checkNotSpyglass"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.SPYGLASS)
        }
        optionsById["checkFioreAppeared"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.FIORE_APPEARED)
        }
        optionsById["checkFioreDidntAppear"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.FIORE_APPEARED)
        }
        optionsById["checkPickedStar"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.PICK_STAR)
        }
        optionsById["checkDidntPickStar"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.PICK_STAR)
        }
        optionsById["checkForcedBoy"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.BOY_FORCED)
        }
        optionsById["checkDidntForceBoy"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.BOY_FORCED)
        }
        optionsById["checkLiedAboutBeetles"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.LIE_ABOUT_BEETLES)
        }
        optionsById["checkLiedAboutRocks"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.LIE_ABOUT_ROCKS)
        }
        optionsById["checkLiedAboutPlants"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.LIE_ABOUT_PLANTS)
        }
        optionsById["checkLibraryRouteAndNoEncounter"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.WHAT_HAPPENED_TO_THE_LIBRARY)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_SHEPHERD_ENCOUNTER)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_HARPY_ENCOUNTER)
        }
        optionsById["checkStormRouteAndNoEncounter"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.WENT_THROUGH_THE_STORM)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_SHEPHERD_ENCOUNTER)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_HARPY_ENCOUNTER)
        }
        optionsById["checkWasInEncounter"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_SHEPHERD_ENCOUNTER)
                || choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_HARPY_ENCOUNTER)
        }
        optionsById["checkWasInShepherdEncounter"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_SHEPHERD_ENCOUNTER)
        }
        optionsById["checkWasntInShepherdEncounter"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_SHEPHERD_ENCOUNTER)
        }
        optionsById["checkWasInHarpyEncounter"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_HARPY_ENCOUNTER)
        }
        optionsById["checkWasntInHarpyEncounter"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.WAS_IN_HARPY_ENCOUNTER)
        }
        optionsById["checkTipMusicians"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.TIP_MUSICIANS)
        }
        optionsById["checkDidntTipMusicians"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.TIP_MUSICIANS)
        }
        optionsById["checkCanEarnMoney"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.CAN_EARN_MONEY)
        }

        optionsById["checkGotNoOne"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_BOY)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_GIRL)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_WIZARD)
        }
        optionsById["checkGotOnlyBoy"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_BOY)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_GIRL)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_WIZARD)
        }
        optionsById["checkGotOnlyGirl"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_GIRL)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_BOY)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_WIZARD)
        }
        optionsById["checkGotOnlyWizard"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_WIZARD)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_GIRL)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_BOY)
        }
        optionsById["checkGotBoyAndGirl"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_GIRL)
                && choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_BOY)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_WIZARD)
        }
        optionsById["checkGotWizardAndGirl"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_GIRL)
                && choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_WIZARD)
                && !choiceService.checkChoiceHasBeenMade(it, Choice.GOT_THE_BOY)
        }
        optionsById["checkShepherdIsFriend"] = {
            counterService.getCounterValue(it, CounterType.BOY_RELATIONSHIP) >= SHEPHERD_IS_FRIEND_MIN_POINTS
        }
        optionsById["checkBadChoices"] = {
            counterService.getCounterValue(it, CounterType.BAD_GUY) == BAD_GUY_POINTS_REQUIRED
        }
    }
}