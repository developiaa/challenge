package pro.developia._2026_06.mock.test4

import org.springframework.stereotype.Service

@Service
class RecommendationService(
    private val watchHistoryRepository: WatchHistoryRepository
) {
    /**
     * 유저의 시청 기록을 기반으로 가장 많이 본 장르 Top 3를 반환합니다.
     *
     * [구현 요구사항]
     * 1. Repository에서 해당 유저의 전체 시청 기록을 가져와 Sequence로 변환합니다.
     * 2. 의미 없는 데이터(시청 시간이 5분 미만인 로그)는 제외합니다.
     * 3. 장르(genre)별로 그룹화합니다.
     * 4. 장르별 총 시청 시간(Int)을 누적하여 계산합니다.
     * 5. 총 시청 시간이 높은 순(내림차순)으로 정렬합니다.
     * 6. 상위 3개의 데이터만 선택(take)합니다.
     * 7. 최종적으로 장르 이름만 추출하여 List<String>으로 반환합니다.
     */
    fun getTop3FavoriteGenres(userId: Long): List<String> {
        return watchHistoryRepository.findLogsByUserId(userId)
            .asSequence()
            .filter { it.watchDurationMinutes >= 5 } // 2. 5분 이상 시청만 유효 데이터로 취급
            .groupBy { it.genre }                    // 3. 장르별 그룹화
            // 직관적인 sumOf 활용
            .mapValues { entry -> entry.value.sumOf { it.watchDurationMinutes } }
            // Map을 다시 Sequence로 변환하여 정렬 가능하게 만듦
            .asSequence()
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
            .toList()
    }
}
