package paket.backend.order

import arrow.core.Either
import arrow.core.flatMap
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.arrow.errorHandler.ErrorMessageService
import paket.backend.database.Item
import paket.backend.database.Order
import paket.backend.database.OrderStatus
import paket.backend.dish.DishRepository
import paket.backend.dtos.PlaceOrderRequestDTO
import paket.backend.user.UserRepository
import java.time.LocalDate

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val dishRepository: DishRepository,
    private val userRepository: UserRepository,
    private val errorMessageService: ErrorMessageService,
) {
    fun allOrders(email: String): Either<AppError, List<Order>> {
        val user = userRepository.findByEmail(email)
        return if (user!!.isAdmin) {
            Either.Right(orderRepository.findAll())
        } else {
            Either.Right(orderRepository.findAllByCreatedById(user.id))
        }
    }

    fun searchOrders(
        email: String,
        statuses: List<OrderStatus>?,
        dateFrom: String?,
        dateTo: String?,
        userId: Long?,
    ): Either<Error, List<Order>> {
        return try {
            var tempF = dateFrom
            var tempT = dateTo
            if (dateFrom!!.isEmpty()) {
                tempF = null
            }
            if (dateTo!!.isEmpty()) {
                tempT = null
            }
            val parsedDateFrom = tempF?.let { LocalDate.parse(it) } ?: LocalDate.MIN
            val parsedDateTo = tempT?.let { LocalDate.parse(it) } ?: LocalDate.now()

            val user =
                userRepository.findByEmail(email)
                    ?: return Either.Left(Error("User not found with email: $email"))

            val userOrders: List<Order> =
                if (!user.isAdmin) {
                    orderRepository.findAllByCreatedById(user.id)
                } else {
                    if (userId == null || userId == -1L) {
                        orderRepository.findAll()
                    } else {
                        orderRepository.findAllByCreatedById(userId)
                    }
                }

            val filteredOrders =
                userOrders.filter { order ->
                    println("Order: ${order.status}")
                    // Check if the order status matches or if no statuses are provided (all statuses)
                    val matchesStatus = statuses == null || order.status in statuses
                    println("Order: ${order.status}")

                    // Check if the order's creation date falls within the provided range
                    val matchesDate =
                        !order.createdAt.isBefore(parsedDateFrom.atStartOfDay()) &&
                            !order.createdAt.isAfter(parsedDateTo.atTime(23, 59, 59))

                    matchesStatus && matchesDate
                }

            Either.Right(filteredOrders)
        } catch (e: Exception) {
            errorMessageService.logError(
                orderId = null, // No specific order ID is relevant here
                operation = "SEARCH ORDER",
                message = "Failed to filter orders: ${e.message}",
            )
            Either.Left(Error("Failed to filter orders: ${e.message}"))
        }
    }

    fun cancelOrder(
        orderId: Long,
        email: String,
    ): Either<AppError, Order> {
        val order = orderRepository.findById(orderId)
        if (order.isEmpty) {
            errorMessageService.logError(
                orderId = orderId,
                operation = "CANCEL ORDER",
                message = "Order not found",
            )
            return Either.Left(AppError.NotFound("Order", orderId))
        }

        val orderEntity = order.get()
        if (orderEntity.createdBy!!.email != email) {
            errorMessageService.logError(
                orderId = orderId,
                operation = "CANCEL ORDER",
                message = "User unauthorized to cancel order",
            )
            return Either.Left(AppError.Unauthorized("Cancel this order"))
        }

        return if (orderEntity.status == OrderStatus.ORDERED) {
            orderEntity.status = OrderStatus.CANCELED
            orderEntity.active = false
            Either.Right(orderRepository.save(orderEntity))
        } else {
            errorMessageService.logError(
                orderId = orderId,
                operation = "ORDER CANCEL",
                message = "Order cannot be canceled unless it is in ORDERED state.",
            )
            Either.Left(AppError.ValidationFailed("Order cannot be canceled unless it is in ORDERED state."))
        }
    }

    fun trackOrder(
        orderId: Long,
        email: String,
    ): Either<AppError, String> {
        val order = orderRepository.findById(orderId)
        if (order.isEmpty) return Either.Left(AppError.NotFound("Order", orderId))

        val orderEntity = order.get()
        if (orderEntity.createdBy!!.email != email) {
            errorMessageService.logError(
                orderId = orderId,
                operation = "TRACKING ORDER",
                message = "User unauthorized to track order",
            )
            return Either.Left(AppError.Unauthorized("track this order"))
        }

        println("Status to return: ${orderEntity.status.name}")
        return Either.Right(orderEntity.status.name)
    }

    fun createOrder(
        email: String,
        request: PlaceOrderRequestDTO,
    ): Either<AppError, Order> {
        // todo gotta check can someone schedule order
        val concurrentOrdersCount =
            orderRepository.countByStatusIn(
                listOf(OrderStatus.ORDERED, OrderStatus.PREPARING, OrderStatus.IN_DELIVERY),
            )
        if (concurrentOrdersCount >= 2) {
            errorMessageService.logError(
                orderId = null,
                operation = "CREATE_ORDER",
                message = "Maximum number of concurrent orders reached.",
            )
            return Either.Left(AppError.ValidationFailed("Maximum number of concurrent orders reached."))
        }

        val userResult =
            userRepository.findByEmail(email)?.let { Either.Right(it) }
                ?: Either.Left(AppError.NotFound("User", 0))
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

    fun recentOrder(email: String): Either<AppError, Order> {
        val user = userRepository.findByEmail(email)
        val orders: List<Order> = orderRepository.findAllByCreatedById(user!!.id)
        println("Last order: ${orders.last()}")
        return Either.Right(orders.last())
    }
}
