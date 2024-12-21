package paket.backend.dtos

import paket.backend.database.Item

data class ItemDTO(
    val id: Long,
    val dish: DishDTO,
    val quantity: Int,
)

fun Item.toDTO() =
    ItemDTO(
        id = this.id,
        dish = this.dish.toDTO(),
        quantity = this.quantity,
    )
