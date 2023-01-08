package ndw.eugene.textgaming.services

import ndw.eugene.textgaming.data.entity.Feedback
import ndw.eugene.textgaming.data.entity.Report
import ndw.eugene.textgaming.data.repository.FeedbackRepository
import ndw.eugene.textgaming.data.repository.GameStateRepository
import ndw.eugene.textgaming.data.repository.ReportRepository
import org.springframework.stereotype.Service

@Service
class FeedbackService(
    private val gameStateRepository: GameStateRepository,
    private val reportRepository: ReportRepository,
    private val feedbackRepository: FeedbackRepository
) {
    
    fun writeReport(userId: Long, reportText: String) {
        val currentGameState = gameStateRepository.findGameStateWithMaxIdByUserId(userId)

        val report = Report()
        report.userId = userId
        report.reportText = reportText
        if (currentGameState != null) {
            report.location = currentGameState.location
            report.conversationId = currentGameState.currentConversationId
        }

        reportRepository.save(report)
    }

    fun writeFeedback(userId: Long, feedbackText: String) {
        val feedback = Feedback()
        feedback.userId = userId
        feedback.feedbackText = feedbackText

        feedbackRepository.save(feedback)
    }
}