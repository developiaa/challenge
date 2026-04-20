package pro.developia._2026_05.purchase.domain.model

import java.math.BigDecimal

data class OrderLineItem(
    val productId: Long,
    val quantity: Int,
    val price: BigDecimal
)
