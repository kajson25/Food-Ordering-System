package paket.backend.order

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import paket.backend.arrow.errorHandler.ErrorMessageService
import paket.backend.database.OrderStatus
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class OrderStatusService(
    private val orderRepository: OrderRepository,
    private val errorMessageService: ErrorMessageService,
) {
    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    @Async
    fun updateOrderStatuses() {
        val now = LocalDateTime.now()
        val orders = orderRepository.findAll()

        orders.forEach { order ->
            when (order.status) {
                OrderStatus.ORDERED -> {
                    if (order.createdAt.isBefore(now.minusSeconds(10 + Random.nextLong(3)))) {
                        order.status = OrderStatus.PREPARING
                        orderRepository.save(order)
                    }
                }
                OrderStatus.PREPARING -> {
                    if (order.createdAt.isBefore(now.minusSeconds(15 + Random.nextLong(3)))) {
                        order.status = OrderStatus.IN_DELIVERY
                        orderRepository.save(order)
                    }
                }
                OrderStatus.IN_DELIVERY -> {
                    if (order.createdAt.isBefore(now.minusSeconds(20 + Random.nextLong(3)))) {
                        order.status = OrderStatus.DELIVERED
                        orderRepository.save(order)
                    }
                }
                else -> {}
            }
        }
    }
}
