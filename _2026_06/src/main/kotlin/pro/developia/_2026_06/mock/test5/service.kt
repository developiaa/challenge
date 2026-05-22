package pro.developia._2026_06.mock.test5

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MaintenanceAlertService(
    private val telemetryRepository: TelemetryRepository
) {
    /**
     * 특정 시간 이후의 로그를 분석하여, CRITICAL 에러가 threshold 이상 발생한 차량 ID를 반환합니다.
     *
     * [구현 요구사항]
     * 1. Repository에서 특정 시간(since) 이후의 로그를 가져와 Sequence로 변환합니다.
     * 2. 심각도(severity)가 CRITICAL인 로그만 필터링합니다.
     * 3. 차량 ID(vehicleId)를 기준으로 데이터를 그룹화합니다.
     * 4. 각 차량별 CRITICAL 로그의 "발생 횟수(개수)"를 구합니다.
     * (힌트: fold로 금액을 더할 필요 없이, 컬렉션의 size나 groupingBy().eachCount()를 활용할 수 있습니다.
     * 단, Sequence의 특성을 유지하려면 groupBy 후 mapValues { it.value.size } 방식이 직관적입니다.)
     * 5. 에러 발생 횟수가 임계치(criticalThreshold) 이상인 차량만 남깁니다.
     * 6. 점검이 필요한 차량 ID만 추출하여 List<Long>으로 반환합니다.
     */
    fun getVehiclesNeedingMaintenance(
        since: LocalDateTime,
        criticalThreshold: Int
    ): List<Long> {
        return telemetryRepository.findLogsSince(since)
            .asSequence()
            .filter { it.severity == Severity.CRITICAL }

            .groupBy { it.vehicleId }
            .mapValues { it.value.size }
            // 위에처럼 사용하거나 아래처럼 사용
//            .groupingBy { it.vehicleId }
//            .eachCount()
//            .asSequence()

            .filter { it.value > criticalThreshold }
            .map { it.key }
            .toList()


    }
}
