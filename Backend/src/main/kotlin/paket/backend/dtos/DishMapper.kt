package paket.backend.dtos

import paket.backend.database.Dish

data class DishDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Double,
)

fun Dish.toDTO() =
    DishDTO(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
    )
