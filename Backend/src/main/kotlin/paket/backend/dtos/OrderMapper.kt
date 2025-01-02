package paket.backend.dtos

import paket.backend.database.Order

data class OrderDTO(
    val id: Long,
    val status: String,
    val active: Boolean,
    val createdBy: String, // user email
    val items: List<ItemDTO>,
)

data class PlaceOrderRequestDTO(
    val dishIds: List<Long>,
    val quantities: List<Int>,
    val scheduledTime: String? = null, // ISO 8601 Format
)

fun Order.toDTO() =
    OrderDTO(
        id = this.id,
        status = this.status.name,
        active = this.active,
        createdBy = this.createdBy!!.email,
        items = this.items.map { it.toDTO() },
    )
