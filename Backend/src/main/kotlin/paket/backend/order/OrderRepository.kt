package paket.backend.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import paket.backend.database.Order
import paket.backend.database.OrderStatus

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByCreatedById(userId: Long): List<Order>

    fun countByStatusIn(statuses: List<OrderStatus>): Int

    fun findAllByStatusInAndCreatedById(
        statuses: List<OrderStatus>,
        userId: Long,
    ): List<Order>
}
