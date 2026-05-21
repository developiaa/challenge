package pro.developia._2026_06.mock.test4

import org.springframework.stereotype.Repository

data class WatchLog(
    val logId: Long,
    val userId: Long,
    val genre: String,
    val watchDurationMinutes: Int // 시청 시간(분)
)

@Repository
interface WatchHistoryRepository {
    fun findLogsByUserId(userId: Long): List<WatchLog>
}
