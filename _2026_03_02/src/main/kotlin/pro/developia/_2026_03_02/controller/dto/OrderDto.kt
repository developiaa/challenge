package pro.developia._2026_03_02.controller.dto

data class OrderCreateRequest(
    val userId: String,
    val productId: String,
)

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val productId: Long,
)
