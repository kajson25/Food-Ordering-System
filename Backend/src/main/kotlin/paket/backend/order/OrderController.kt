@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.order

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paket.backend.arrow.ApiResponse
import paket.backend.database.OrderStatus
import paket.backend.dtos.OrderDTO
import paket.backend.dtos.PlaceOrderRequestDTO
import paket.backend.dtos.toDTO

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @GetMapping
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

    @PostMapping
    fun placeOrder(
        @RequestHeader("userId") userId: Long,
        @RequestBody request: PlaceOrderRequestDTO,
    ): ResponseEntity<ApiResponse<OrderDTO>> =
        orderService.placeOrder(userId, request).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { order -> ResponseEntity.ok(ApiResponse(success = true, data = order.toDTO())) },
        )
}
