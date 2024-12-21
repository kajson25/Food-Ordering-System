@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.order

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paket.backend.arrow.ApiResponse
import paket.backend.database.OrderStatus
import paket.backend.dtos.OrderDTO
import paket.backend.dtos.PlaceOrderRequestDTO
import paket.backend.dtos.toDTO
import java.time.LocalDateTime

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
    private val scheduledOrderService: ScheduledOrderService,
) {
    @GetMapping("/all")
    fun allOrders(
        @RequestParam userId: Long?,
    ): ResponseEntity<ApiResponse<List<OrderDTO>>> =
        orderService.allOrders(userId!!).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { orders -> ResponseEntity.ok(ApiResponse(success = true, data = orders.map { it.toDTO() })) },
        )

    @GetMapping("/search")
    fun searchOrders(
        @RequestParam userId: Long?,
        @RequestParam statuses: List<OrderStatus>?,
        @RequestParam dateFrom: String?,
        @RequestParam dateTo: String?,
    ): ResponseEntity<ApiResponse<List<OrderDTO>>> =
        orderService.searchOrders(userId, statuses, dateFrom, dateTo).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { orders -> ResponseEntity.ok(ApiResponse(success = true, data = orders.map { it.toDTO() })) },
        )

    @PostMapping("/new-order")
    fun placeOrder(
        @RequestParam userId: Long,
        @RequestBody request: PlaceOrderRequestDTO,
    ): ResponseEntity<ApiResponse<OrderDTO>> =
        orderService.placeOrder(userId, request).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { order -> ResponseEntity.ok(ApiResponse(success = true, data = order.toDTO())) },
        )

    @PostMapping("/{orderId}/cancel")
    fun cancelOrder(
        @RequestParam userId: Long,
        @PathVariable orderId: Long,
    ): ResponseEntity<ApiResponse<OrderDTO>> =
        orderService.cancelOrder(orderId, userId).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { order -> ResponseEntity.ok(ApiResponse(success = true, data = order.toDTO())) },
        )

    @GetMapping("/{orderId}/track")
    fun trackOrder(
        @RequestParam userId: Long,
        @PathVariable orderId: Long,
    ): ResponseEntity<ApiResponse<String>> =
        orderService.trackOrder(orderId, userId).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { status -> ResponseEntity.ok(ApiResponse(success = true, data = status)) },
        )

    @PostMapping("/schedule")
    fun scheduleOrder(
        @RequestParam userId: Long,
        @RequestBody request: PlaceOrderRequestDTO,
    ): ResponseEntity<ApiResponse<String>> =
        scheduledOrderService.scheduleOrder(userId, request.dishIds, LocalDateTime.parse(request.scheduledTime!!)).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { message -> ResponseEntity.ok(ApiResponse(success = true, data = message)) },
        )
}
