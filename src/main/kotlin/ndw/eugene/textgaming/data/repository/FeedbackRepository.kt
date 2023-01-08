package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.Feedback
import org.springframework.data.repository.CrudRepository

interface FeedbackRepository : CrudRepository<Feedback, Long>