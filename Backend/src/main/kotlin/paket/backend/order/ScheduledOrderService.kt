package paket.backend.order

import arrow.core.Either
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.arrow.errorHandler.ErrorMessageService
import paket.backend.database.Item
import paket.backend.database.Order
import paket.backend.database.OrderStatus
import paket.backend.dish.DishRepository
import paket.backend.user.UserService
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class ScheduledOrderService(
    private val orderRepository: OrderRepository,
    private val userService: UserService,
    private val dishRepository: DishRepository,
    private val errorMessageService: ErrorMessageService,
) {
    private val scheduledOrders = ConcurrentLinkedQueue<ScheduledOrder>()

    data class ScheduledOrder(
        val email: String,
        val dishIds: List<Long>,
        val scheduledTime: LocalDateTime,
    )

    fun scheduleOrder(
        email: String,
        dishIds: List<Long>,
        scheduledTime: LocalDateTime,
    ): Either<AppError, String> {
        if (scheduledTime.isBefore(LocalDateTime.now())) {
            return Either.Left(AppError.ValidationFailed("Scheduled time cannot be in the past."))
        }

        scheduledOrders.add(ScheduledOrder(email, dishIds, scheduledTime))
        return Either.Right("Order successfully scheduled.")
    }

    @Scheduled(fixedRate = 60000) // Runs every minute
    @Async
    fun processScheduledOrders() {
        val now = LocalDateTime.now()

        scheduledOrders.filter { it.scheduledTime.isBefore(now) }.forEach { scheduledOrder ->
            val userResult = userService.getUserByEmail(scheduledOrder.email)
            val dishes =
                scheduledOrder.dishIds.mapNotNull { dishId ->
                    dishRepository.findById(dishId).orElse(null)
                }

            if (userResult.isRight() && dishes.size == scheduledOrder.dishIds.size) {
                val user = userResult.getOrNull()!!

                val items = dishes.map { dish -> Item(dish = dish, quantity = 1) }
                val order =
                    Order(
                        status = OrderStatus.ORDERED,
                        createdBy = user,
                        active = true,
                        items = items.toMutableList(),
                    )

                try {
                    orderRepository.save(order)
                } catch (ex: Exception) {
                    errorMessageService.logError(
                        orderId = null,
                        operation = "SCHEDULE",
                        message = "Failed to create order: ${ex.message}",
                    )
                }
            } else {
                errorMessageService.logError(
                    orderId = null,
                    operation = "SCHEDULE",
                    message = "Failed to create order due to missing user or dishes.",
                )
            }

            scheduledOrders.remove(scheduledOrder)
        }
    }
}
