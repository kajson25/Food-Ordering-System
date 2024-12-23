package paket.backend.order

import arrow.core.Either
import arrow.core.flatMap
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.arrow.ErrorMessageService
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
    private val errorMessageService: ErrorMessageService,
) {
    fun allOrders(userId: Long): Either<AppError, List<Order>> = Either.Right(orderRepository.findAll())

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

    fun cancelOrder(
        orderId: Long,
        userId: Long,
    ): Either<AppError, Order> {
        val order = orderRepository.findById(orderId)
        if (order.isEmpty) return Either.Left(AppError.NotFound("Order", orderId))

        val orderEntity = order.get()
        if (orderEntity.createdBy!!.id != userId) return Either.Left(AppError.Unauthorized("cancel this order"))

        return if (orderEntity.status == OrderStatus.ORDERED) {
            orderEntity.status = OrderStatus.CANCELED
            Either.Right(orderRepository.save(orderEntity))
        } else {
            Either.Left(AppError.ValidationFailed("Order cannot be canceled unless it is in ORDERED state."))
        }
    }

    fun trackOrder(
        orderId: Long,
        userId: Long,
    ): Either<AppError, String> {
        val order = orderRepository.findById(orderId)
        if (order.isEmpty) return Either.Left(AppError.NotFound("Order", orderId))

        val orderEntity = order.get()
        if (orderEntity.createdBy!!.id != userId) return Either.Left(AppError.Unauthorized("track this order"))

        return Either.Right(orderEntity.status.name)
    }

    fun createOrder(
        userId: Long,
        request: PlaceOrderRequestDTO,
    ): Either<AppError, Order> {
        val concurrentOrdersCount = orderRepository.countByStatusIn(listOf(OrderStatus.PREPARING, OrderStatus.IN_DELIVERY))
        if (concurrentOrdersCount >= 3) {
            errorMessageService.logError(
                orderId = null,
                operation = "CREATE_ORDER",
                message = "Maximum number of concurrent orders reached.",
            )
            return Either.Left(AppError.ValidationFailed("Maximum number of concurrent orders reached."))
        }

        val userResult = userService.getUserById(userId)
        return userResult.flatMap { user ->
            val dishes =
                request.dishIds.mapNotNull { dishId ->
                    dishRepository.findById(dishId).orElse(null)
                }
            if (dishes.size != request.dishIds.size) {
                Either.Left(AppError.ValidationFailed("Some dishes do not exist."))
            } else {
                val items =
                    dishes.map { dish ->
                        Item(
                            dish = dish,
                            quantity = request.quantities[request.dishIds.indexOf(dish.id)],
                        )
                    }
                val order =
                    Order(
                        status = OrderStatus.ORDERED,
                        createdBy = user,
                        active = true,
                        items = items.toMutableList(),
                    )
                items.map { it.order = order }
                println("Order to save: $order")
                Either.Right(orderRepository.save(order))
            }
        }
    }
}
