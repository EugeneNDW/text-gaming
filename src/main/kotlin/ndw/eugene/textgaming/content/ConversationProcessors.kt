package ndw.eugene.textgaming.content

import ndw.eugene.textgaming.data.entity.CounterType
import ndw.eugene.textgaming.data.entity.GameState
import ndw.eugene.textgaming.services.*
import org.springframework.stereotype.Component

private const val BAD_GUY_POINTS_REQUIRED = 5
private const val SHEPHERD_IS_FRIEND_MIN_POINTS = 3

@Component
class ConversationProcessors(
    private val choiceService: ChoiceService,
    private val locationService: LocationService,
    private val counterService: CounterService,
) {
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

    private val conditionMap: Map<String, String> = mapOf(
        "secondOptionChosen" to "${CHECK_CONDITION_PREFIX}:${Choice.TEST}",
        "retellCompanionStoryCheck" to "${CHECK_CONDITION_PREFIX}:${Choice.COMPANION_STORY}",
        "checkStarsTalking" to "${CHECK_CONDITION_PREFIX}:${Choice.STARS_TALKING}",
        "checkStarsSilent" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.STARS_TALKING}",
        "checkOutOfOffers" to "${CHECK_CONDITION_PREFIX}:${Choice.OFFER_SOLACE} && ${CHECK_CONDITION_PREFIX}:${Choice.OFFER_TRIBUTE} && ${CHECK_CONDITION_PREFIX}:${Choice.OFFER_HOPE}",
        "checkStillHaveOffers" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.OFFER_SOLACE} || ${CHECK_NOT_CONDITION_PREFIX}:${Choice.OFFER_TRIBUTE} || ${CHECK_NOT_CONDITION_PREFIX}:${Choice.OFFER_HOPE}",
        "checkTonicWasBought" to "${CHECK_CONDITION_PREFIX}:${Choice.TONIC_BOUGHT}",
        "checkDidntHearAboutFather" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.HEARD_ABOUT_FATHER}",
        "checkHeardAboutFather" to "${CHECK_CONDITION_PREFIX}:${Choice.HEARD_ABOUT_FATHER}",
        "checkTidyYourself" to "${CHECK_CONDITION_PREFIX}:${Choice.TIDY_YOURSELF}",
        "checkNotTidyYourself" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.TIDY_YOURSELF}",
        "checkLostName" to "${CHECK_CONDITION_PREFIX}:${Choice.LOST_NAME}",
        "checkHaveName" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.LOST_NAME}",
        "checkHeardSadSong" to "${CHECK_CONDITION_PREFIX}:${Choice.SAD_SONG}",
        "checkHeardEpicSong" to "${CHECK_CONDITION_PREFIX}:${Choice.EPIC_SONG}",
        "checkDidntHearEpicSong" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.EPIC_SONG}",
        "checkHeardFunnySong" to "${CHECK_CONDITION_PREFIX}:${Choice.FUNNY_SONG}",
        "checkHeardMerchantStory" to "${CHECK_CONDITION_PREFIX}:${Choice.HEARD_MERCHANT_STORY}",
        "checkHaveNoStoryToTell" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.HEARD_MERCHANT_STORY} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.SAD_SONG} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.EPIC_SONG} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.FUNNY_SONG} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.COMPANION_STORY}",
        "checkAskedAboutResearch" to "${CHECK_CONDITION_PREFIX}:${Choice.RESEARCH}",
        "checkKnowsAboutLibrary" to "${CHECK_CONDITION_PREFIX}:${Choice.WHAT_HAPPENED_TO_THE_LIBRARY}",
        "checkDontKnowAboutLibrary" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.WHAT_HAPPENED_TO_THE_LIBRARY}",
        "checkDestroyed" to "${CHECK_CONDITION_PREFIX}:${Choice.DESTROYED_HOW}",
        "checkNotDestroyed" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.DESTROYED_HOW}",
        "checkLibrarians" to "${CHECK_CONDITION_PREFIX}:${Choice.LIBRARIANS}",
        "checkNotLibrarians" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.LIBRARIANS}",
        "checkMetTheHarpy" to "${CHECK_CONDITION_PREFIX}:${Choice.MET_THE_HARPY}",
        "checkDidntMeetTheHarpy" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.MET_THE_HARPY}",
        "checkWentThroughStorm" to "${CHECK_CONDITION_PREFIX}:${Choice.WENT_THROUGH_THE_STORM}",
        "checkDidntGoThroughStorm" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.WENT_THROUGH_THE_STORM}",
        "checkStatuesNotExamined" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.EXAMINED_STATUE}",
        "checkStatueExamined" to "${CHECK_CONDITION_PREFIX}:${Choice.EXAMINED_STATUE}",
        "checkShelvesNotExamined" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.EXAMINED_SHELVES}",
        "checkShelvesExamined" to "${CHECK_CONDITION_PREFIX}:${Choice.EXAMINED_SHELVES}",
        "checkLeftShepherdToLibrary" to "${CHECK_CONDITION_PREFIX}:${Choice.LEFT_SHEPHERD_TO_LIBRARY}",
        "checkLeftShepherdToHarpies" to "${CHECK_CONDITION_PREFIX}:${Choice.LEFT_SHEPHERD_TO_HARPIES}",
        "checkLeftShepherdToJungle" to "${CHECK_CONDITION_PREFIX}:${Choice.LEFT_SHEPHERD_TO_JUNGLE}",
        "checkGaveShepherdTonic" to "${CHECK_CONDITION_PREFIX}:${Choice.GIVE_TONIC_TO_SHEPHERD}",
        "checkSpyglass" to "${CHECK_CONDITION_PREFIX}:${Choice.SPYGLASS}",
        "checkNotSpyglass" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.SPYGLASS}",
        "checkFioreAppeared" to "${CHECK_CONDITION_PREFIX}:${Choice.FIORE_APPEARED}",
        "checkFioreDidntAppear" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.FIORE_APPEARED}",
        "checkPickedStar" to "${CHECK_CONDITION_PREFIX}:${Choice.PICK_STAR}",
        "checkDidntPickStar" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.PICK_STAR}",
        "checkForcedBoy" to "${CHECK_CONDITION_PREFIX}:${Choice.BOY_FORCED}",
        "checkDidntForceBoy" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.BOY_FORCED}",
        "checkLiedAboutBeetles" to "${CHECK_CONDITION_PREFIX}:${Choice.LIE_ABOUT_BEETLES}",
        "checkLiedAboutRocks" to "${CHECK_CONDITION_PREFIX}:${Choice.LIE_ABOUT_ROCKS}",
        "checkLiedAboutPlants" to "${CHECK_CONDITION_PREFIX}:${Choice.LIE_ABOUT_PLANTS}",
        "checkLibraryRouteAndNoEncounter" to "${CHECK_CONDITION_PREFIX}:${Choice.WHAT_HAPPENED_TO_THE_LIBRARY} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.WAS_IN_SHEPHERD_ENCOUNTER} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.WAS_IN_HARPY_ENCOUNTER}",
        "checkStormRouteAndNoEncounter" to "${CHECK_CONDITION_PREFIX}:${Choice.WENT_THROUGH_THE_STORM} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.WAS_IN_SHEPHERD_ENCOUNTER} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.WAS_IN_HARPY_ENCOUNTER}",
        "checkWasInEncounter" to "${CHECK_CONDITION_PREFIX}:${Choice.WAS_IN_SHEPHERD_ENCOUNTER} || ${CHECK_CONDITION_PREFIX}:${Choice.WAS_IN_HARPY_ENCOUNTER}",
        "checkWasInShepherdEncounter" to "${CHECK_CONDITION_PREFIX}:${Choice.WAS_IN_SHEPHERD_ENCOUNTER}",
        "checkWasntInShepherdEncounter" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.WAS_IN_SHEPHERD_ENCOUNTER}",
        "checkWasInHarpyEncounter" to "${CHECK_CONDITION_PREFIX}:${Choice.WAS_IN_HARPY_ENCOUNTER}",
        "checkWasntInHarpyEncounter" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.WAS_IN_HARPY_ENCOUNTER}",
        "checkTipMusicians" to "${CHECK_CONDITION_PREFIX}:${Choice.TIP_MUSICIANS}",
        "checkDidntTipMusicians" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.TIP_MUSICIANS}",
        "checkCanEarnMoney" to "${CHECK_CONDITION_PREFIX}:${Choice.CAN_EARN_MONEY}",
        "checkCantEarnMoney" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.CAN_EARN_MONEY}",
        "checkHeardAboutProphet" to "${CHECK_CONDITION_PREFIX}:${Choice.HEARD_ABOUT_PROPHET}",
        "checkDidntHearAboutProphet" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.HEARD_ABOUT_PROPHET}",
        "checkGotNoOne" to "${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_BOY} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_GIRL} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_WIZARD}",
        "checkGotOnlyBoy" to "${CHECK_CONDITION_PREFIX}:${Choice.GOT_THE_BOY} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_GIRL} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_WIZARD}",
        "checkGotOnlyGirl" to "${CHECK_CONDITION_PREFIX}:${Choice.GOT_THE_GIRL} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_BOY} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_WIZARD}",
        "checkGotOnlyWizard" to "${CHECK_CONDITION_PREFIX}:${Choice.GOT_THE_WIZARD} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_GIRL} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_BOY}",
        "checkGotBoyAndGirl" to "${CHECK_CONDITION_PREFIX}:${Choice.GOT_THE_GIRL} && ${CHECK_CONDITION_PREFIX}:${Choice.GOT_THE_BOY} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_WIZARD}",
        "checkGotWizardAndGirl" to "${CHECK_CONDITION_PREFIX}:${Choice.GOT_THE_GIRL} && ${CHECK_CONDITION_PREFIX}:${Choice.GOT_THE_WIZARD} && ${CHECK_NOT_CONDITION_PREFIX}:${Choice.GOT_THE_BOY}",
        "checkShepherdIsFriend" to "${MORETHAN_CONDITION_PREFIX}:$SHEPHERD_IS_FRIEND_MIN_POINTS:${CounterType.BOY_RELATIONSHIP}",
        "checkBadChoices" to "${EQUALS_CONDITION_PREFIX}:$BAD_GUY_POINTS_REQUIRED:${CounterType.BAD_GUY}"
    )

    fun getOptionString(id: String?): String {
        if (id.isNullOrBlank()) return ""

        return conditionMap[id] ?: throw IllegalArgumentException("there is no option with id: $id")
    }

    fun getProcessorById(id: String): String {
        if (id.isBlank()) return ""

        return processorMap[id] ?: throw IllegalArgumentException("there is no processor with id: $id")
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