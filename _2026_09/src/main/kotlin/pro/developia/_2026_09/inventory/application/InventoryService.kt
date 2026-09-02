package pro.developia._2026_09.inventory.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_09.inventory.domain.Inventory

@Service
class InventoryService(
    // private val inventoryRepository: InventoryRepository // DB 통신 생략
) {
    @Transactional
    fun decreaseStock(productId: String, quantity: Int) {
        // 1. DB에서 재고 조회 (비관적 락 등 동시성 제어 적용 권장)
        // val inventory = inventoryRepository.findByProductId(productId) ?: throw Exception("재고 정보 없음")

        // --- 가상의 조회 객체 ---
        val inventory = Inventory(productId, 100)

        // 2. 도메인 로직 호출
        inventory.decrease(quantity)

        // 3. DB 저장
        // inventoryRepository.save(inventory)
        println("[$productId] 재고 ${quantity}개 차감 완료. 남은 재고: ${inventory.availableQuantity}")
    }
}
