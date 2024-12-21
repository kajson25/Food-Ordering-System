package paket.backend.dtos

import paket.backend.database.Order

data class OrderDTO(
    val id: Long,
    val status: String,
    val active: Boolean,
    val createdBy: UserDTO,
    val items: List<ItemDTO>,
)

data class PlaceOrderRequestDTO(
    val dishIds: List<Long>,
    val scheduledTime: String? = null, // ISO 8601 Format
)

fun Order.toDTO() =
    OrderDTO(
        id = this.id,
        status = this.status.name,
        active = this.active,
        createdBy = this.createdBy!!.toDTO(),
        items = this.items.map { it.toDTO() },
    )
