@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.order

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paket.backend.arrow.ApiResponse
import paket.backend.database.OrderStatus
import paket.backend.dtos.OrderDTO
import paket.backend.dtos.PlaceOrderRequestDTO
import paket.backend.dtos.toDTO
import paket.backend.permissions.PermissionMiddleware
import java.time.LocalDateTime

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
    private val scheduledOrderService: ScheduledOrderService,
    private val permissionMiddleware: PermissionMiddleware,
) {
    @GetMapping("/all")
    fun allOrders(requestHttp: HttpServletRequest): ResponseEntity<ApiResponse<List<OrderDTO>>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return orderService.allOrders(userId).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { orders -> ResponseEntity.ok(ApiResponse(success = true, data = orders.map { it.toDTO() })) },
        )
    }

    @GetMapping("/search")
    fun searchOrders(
        @RequestParam statuses: List<OrderStatus>?,
        @RequestParam dateFrom: String?,
        @RequestParam dateTo: String?,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<List<OrderDTO>>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(userId, "CAN_SEARCH_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                orderService.searchOrders(userId, statuses, dateFrom, dateTo).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { orders ->
                        ResponseEntity.ok(
                            ApiResponse(
                                success = true,
                                data = orders.map { it.toDTO() },
                            ),
                        )
                    },
                )
            },
        )
    }

    // tested - working
    @PostMapping("/new-order")
    fun placeOrder(
        @RequestBody request: PlaceOrderRequestDTO,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<OrderDTO>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(userId, "CAN_PLACE_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                orderService.createOrder(userId, request).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { order -> ResponseEntity.ok(ApiResponse(success = true, data = order.toDTO())) },
                )
            },
        )
    }

    // working but it's hard to test happy case because lag is too small
    @PostMapping("/{orderId}/cancel")
    fun cancelOrder(
        @PathVariable orderId: Long,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<OrderDTO>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(userId, "CAN_CANCEL_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                orderService.cancelOrder(orderId, userId).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { order -> ResponseEntity.ok(ApiResponse(success = true, data = order.toDTO())) },
                )
            },
        )
    }

    // tested - works
    @GetMapping("/{orderId}/track")
    fun trackOrder(
        @PathVariable orderId: Long,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<String>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(userId, "CAN_TRACK_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                orderService.trackOrder(orderId, userId).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { status -> ResponseEntity.ok(ApiResponse(success = true, data = status)) },
                )
            },
        )
    }

    // works - no-permission, schedule before today
    @PostMapping("/schedule")
    fun scheduleOrder(
        @RequestBody request: PlaceOrderRequestDTO,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<String>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(userId, "CAN_SCHEDULE_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                scheduledOrderService
                    .scheduleOrder(
                        userId,
                        request.dishIds,
                        LocalDateTime.parse(request.scheduledTime!!),
                    ).fold(
                        ifLeft = { error ->
                            ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                        },
                        ifRight = { message -> ResponseEntity.ok(ApiResponse(success = true, data = message)) },
                    )
            },
        )
    }
}
