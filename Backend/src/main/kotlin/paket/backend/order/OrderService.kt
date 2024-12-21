package paket.backend.order

import arrow.core.Either
import arrow.core.flatMap
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.database.Item
import paket.backend.database.Order
import paket.backend.database.OrderStatus
import paket.backend.dish.DishRepository
import paket.backend.dtos.PlaceOrderRequestDTO
import paket.backend.user.UserService

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val dishRepository: DishRepository,
    private val userService: UserService,
) {
    fun searchOrders(
        userId: Long?,
        status: List<OrderStatus>?,
        dateFrom: String?,
        dateTo: String?,
    ): Either<AppError, List<Order>> =
        Either.Right(
            if (userId != null) {
                orderRepository.findAllByCreatedById(userId)
            } else {
                orderRepository.findAll()
            },
        )

    fun placeOrder(
        userId: Long,
        request: PlaceOrderRequestDTO,
    ): Either<AppError, Order> {
        val userResult = userService.findById(userId)
        return userResult.flatMap { user ->
            val dishes =
                request.dishIds.mapNotNull { dishId ->
                    dishRepository.findById(dishId).orElse(null)
                }
            if (dishes.size != request.dishIds.size) {
                Either.Left(AppError.ValidationFailed("Some dishes do not exist."))
            } else {
                val items = dishes.map { dish -> Item(dish = dish, quantity = 1) }
                val order =
                    Order(
                        status = OrderStatus.ORDERED,
                        createdBy = user,
                        active = true,
                        items = items.toMutableList(),
                    )
                Either.Right(orderRepository.save(order))
            }
        }
    }
}
