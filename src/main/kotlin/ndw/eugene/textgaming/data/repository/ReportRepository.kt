package ndw.eugene.textgaming.data.repository

import ndw.eugene.textgaming.data.entity.Report
import org.springframework.data.repository.CrudRepository

interface ReportRepository : CrudRepository<Report, Long>