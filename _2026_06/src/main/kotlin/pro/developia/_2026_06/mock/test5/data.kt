package pro.developia._2026_06.mock.test5

import org.springframework.stereotype.Repository
import java.time.LocalDateTime

enum class Severity {
    INFO, WARNING, CRITICAL
}

data class TelemetryLog(
    val logId: Long,
    val vehicleId: Long,
    val severity: Severity,
    val timestamp: LocalDateTime
)

@Repository
interface TelemetryRepository {
    fun findLogsSince(since: LocalDateTime): List<TelemetryLog>
}
