package pro.developia._2026_09.inventory.domain

class Inventory(
    val productId: String,
    var availableQuantity: Int
) {
    fun decrease(quantity: Int) {
        require(quantity > 0) { "차감 수량은 0보다 커야 합니다." }
        if (this.availableQuantity < quantity) {
            throw IllegalStateException("재고가 부족합니다. (현재: $availableQuantity, 요청: $quantity)")
        }
        this.availableQuantity -= quantity
    }
}
