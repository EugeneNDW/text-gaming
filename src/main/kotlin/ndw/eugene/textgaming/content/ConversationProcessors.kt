package ndw.eugene.textgaming.content

import mu.KotlinLogging
import ndw.eugene.textgaming.data.entity.CounterType
import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.services.ChoiceService
import ndw.eugene.textgaming.services.CounterService
import ndw.eugene.textgaming.services.LocationService
import org.springframework.stereotype.Component
import java.lang.Exception

typealias OptionCondition = (GameState) -> Boolean

private const val BAD_GUY_POINTS_REQUIRED = 5
private const val SHEPHERD_IS_FRIEND_MIN_POINTS = 3

@Component
class ConversationProcessors(
    private val choiceService: ChoiceService,
    private val locationService: LocationService,
    private val counterService: CounterService,
) {
    private val optionsById = mutableMapOf<String, OptionCondition>()

    private val processorMap: Map<String, String> = mapOf(
        "changeLocationToAlleyways" to "CHANGE:ALLEYWAYS",
        "changeLocationToMarket" to "CHANGE:MARKET",
        "changeLocationToTower" to "CHANGE:TOWER",
        "changeLocationToDome" to "CHANGE:DOME",
        "changeLocationToShip" to "CHANGE:SHIP",
        "changeLocationToStorm" to "CHANGE:STORM",
        "changeLocationToSunkenLibrary" to "CHANGE:SUNKEN_LIBRARY_SHORE",
        "changeLocationToJungle" to "CHANGE:JUNGLE",
        "changeLocationToHarpyEncounter" to "CHANGE:HARPY_ENCOUNTER",
        "changeLocationToShepherdEncounter" to "CHANGE:SHEPHERD_ENCOUNTER",
        "changeLocationToSunkenLibraryInside" to "CHANGE:SUNKEN_LIBRARY_INSIDE",
        "changeLocationToHarpiesLair" to "CHANGE:HARPIES_LAIR",
        "changeLocationToBoyEpilogue" to "CHANGE:BOY_EPILOGUE",
        "changeLocationToWizardEpilogue" to "CHANGE:WIZARD_EPILOGUE",
        "changeLocationToTowerEpilogue" to "CHANGE:TOWER_EPILOGUE",
        "changeLocationToShipEpilogue" to "CHANGE:SHIP_EPILOGUE",
        "memorizeFioreAppeared" to "MEMORIZE:FIORE_APPEARED",
        "memorizeTonicBought" to "MEMORIZE:TONIC_BOUGHT",
        "memorizeCompanionStory" to "MEMORIZE:COMPANION_STORY",
        "memorizeStarsTalking" to "MEMORIZE:STARS_TALKING",
        "testProcessor" to "MEMORIZE:TEST",
        "memorizeOfferSolace" to "MEMORIZE:OFFER_SOLACE",
        "memorizeOfferTribute" to "MEMORIZE:OFFER_TRIBUTE",
        "memorizeOfferHope" to "MEMORIZE:OFFER_HOPE",
        "memorizeHeardAboutFather" to "MEMORIZE:HEARD_ABOUT_FATHER",
        "memorizeLostName" to "MEMORIZE:LOST_NAME",
        "memorizeTidyYourself" to "MEMORIZE:TIDY_YOURSELF",
        "memorizeHeardMerchantStory" to "MEMORIZE:HEARD_MERCHANT_STORY",
        "memorizeSadSong" to "MEMORIZE:SAD_SONG",
        "increaseBoyRelationshipCounter" to "INCREASE:BOY_RELATIONSHIP",
        "decreaseBoyRelationshipCounter" to "DECREASE:BOY_RELATIONSHIP",
        "increaseBadGuyCounter" to "INCREASE:BAD_GUY",
        "decreaseBadGuyCounter" to "DECREASE:BAD_GUY",
        "endGame" to "END:GAME",
        "memorizeFunnySong" to "MEMORIZE:FUNNY_SONG",
        "memorizeEpicSong" to "MEMORIZE:EPIC_SONG",
        "memorizeResearch" to "MEMORIZE:RESEARCH",
        "memorizeWentThroughStorm" to "MEMORIZE:WENT_THROUGH_THE_STORM",
        "memorizeWhatHappenedToTheLibrary" to "MEMORIZE:WHAT_HAPPENED_TO_THE_LIBRARY",
        "memorizeDestroyed" to "MEMORIZE:DESTROYED_HOW",
        "memorizeLibrarians" to "MEMORIZE:LIBRARIANS",
        "memorizeSpyglass" to "MEMORIZE:SPYGLASS",
        "memorizeExaminedStatue" to "MEMORIZE:EXAMINED_STATUE",
        "memorizeExaminedShelves" to "MEMORIZE:EXAMINED_SHELVES",
        "memorizePickStar" to "MEMORIZE:PICK_STAR",
        "memorizeWasInHarpyEncounter" to "MEMORIZE:WAS_IN_HARPY_ENCOUNTER",
        "memorizeWasInShepherdEncounter" to "MEMORIZE:WAS_IN_SHEPHERD_ENCOUNTER",
        "memorizeLiedAboutPlants" to "MEMORIZE:LIE_ABOUT_PLANTS",
        "memorizeLiedAboutRocks" to "MEMORIZE:LIE_ABOUT_ROCKS",
        "memorizeLiedAboutBeetles" to "MEMORIZE:LIE_ABOUT_BEETLES",
        "memorizeLeftShepherdToLibrary" to "MEMORIZE:LEFT_SHEPHERD_TO_LIBRARY",
        "memorizeLeftShepherdToHarpies" to "MEMORIZE:LEFT_SHEPHERD_TO_HARPIES",
        "memorizeLeftShepherdToJungle" to "MEMORIZE:LEFT_SHEPHERD_TO_JUNGLE",
        "memorizeGaveShepherdTonic" to "MEMORIZE:GIVE_TONIC_TO_SHEPHERD",
        "memorizeTipMusicians" to "MEMORIZE:TIP_MUSICIANS",
        "memorizeGotWizard" to "MEMORIZE:GOT_THE_WIZARD",
        "memorizeGotBoy" to "MEMORIZE:GOT_THE_BOY",
        "memorizeGotGirl" to "MEMORIZE:GOT_THE_GIRL",
        "memorizeCanEarnMoney" to "MEMORIZE:CAN_EARN_MONEY",
        "memorizeHeardAboutProphet" to "MEMORIZE:HEARD_ABOUT_PROPHET",
        "memorizeBoyForced" to "MEMORIZE:BOY_FORCED"
    )

    init {
        initOptionConditions()
    }

    fun getOptionConditionById(id: String?): OptionCondition {
        if (id.isNullOrBlank()) return { true }

        return optionsById[id] ?: throw IllegalArgumentException("there is no option with id: $id")
    }

    fun getProcessorById(id: String): String {
        if (id.isBlank()) return ""

        return processorMap[id] ?: throw IllegalArgumentException("there is no processor with id: $id")
    }

    private fun initOptionConditions() {
        optionsById["secondOptionChosen"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.TEST)
        }
        optionsById["retellCompanionStoryCheck"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.COMPANION_STORY)
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
            choiceService.checkChoiceHasBeenMade(it, Choice.TONIC_BOUGHT)
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
            choiceService.checkChoiceHasBeenMade(it, Choice.HEARD_MERCHANT_STORY)
        }
        optionsById["checkHaveNoStoryToTell"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.HEARD_MERCHANT_STORY)
                    && !choiceService.checkChoiceHasBeenMade(it, Choice.SAD_SONG)
                    && !choiceService.checkChoiceHasBeenMade(it, Choice.EPIC_SONG)
                    && !choiceService.checkChoiceHasBeenMade(it, Choice.FUNNY_SONG)
                    && !choiceService.checkChoiceHasBeenMade(it, Choice.COMPANION_STORY)
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
            choiceService.checkChoiceHasBeenMade(it, Choice.DESTROYED_HOW)
        }
        optionsById["checkNotDestroyed"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.DESTROYED_HOW)
        }
        optionsById["checkLibrarians"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.LIBRARIANS)
        }
        optionsById["checkNotLibrarians"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.LIBRARIANS)
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
        optionsById["checkCantEarnMoney"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.CAN_EARN_MONEY)
        }
        optionsById["checkHeardAboutProphet"] = {
            choiceService.checkChoiceHasBeenMade(it, Choice.HEARD_ABOUT_PROPHET)
        }
        optionsById["checkDidntHearAboutProphet"] = {
            !choiceService.checkChoiceHasBeenMade(it, Choice.HEARD_ABOUT_PROPHET)
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

    fun executeProcessor(gameState: GameState, processorString: String) {
        if (processorString.isBlank()) {
            return
        }
        val (action, value) = processorString.split(':').let { it[0] to it[1] }
        when (action) {
            "CHANGE" -> changeLocationTo(gameState, value)
            "MEMORIZE" -> addChoice(gameState, value)
            "INCREASE" -> increaseCounter(gameState, value)
            "DECREASE" -> decreaseCounter(gameState, value)
            "END" -> endGame(gameState)
            else -> println("Unknown action: $action and value: $value")
        }
    }

    private fun changeLocationTo(gameState: GameState, location: String) {
        locationService.changeLocationTo(gameState, location)
    }

    private fun addChoice(gameState: GameState, choice: String) {
        choiceService.addChoice(gameState, choice)
    }

    private fun increaseCounter(gameState: GameState, counter: String) {
        counterService.increaseCounter(gameState, counter)
    }

    private fun decreaseCounter(gameState: GameState, counter: String) {
        counterService.decreaseCounter(gameState, counter)
    }

    private fun endGame(gameState: GameState) {
        gameState.isEnded = true
        locationService.changeLocationTo(gameState, "END")
    }
}