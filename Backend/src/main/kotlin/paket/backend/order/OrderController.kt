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
    fun allOrders(
        @RequestParam email: String,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<List<OrderDTO>>> {
//        val userId = requestHttp.getAttribute("userId") as Long
        return orderService.allOrders(email).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { orders -> ResponseEntity.ok(ApiResponse(success = true, data = orders.map { it.toDTO() })) },
        )
    }

    @GetMapping("/search")
    fun searchOrders(
        @RequestParam email: String,
        @RequestParam statuses: List<OrderStatus>?,
        @RequestParam dateFrom: String?,
        @RequestParam dateTo: String?,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<List<OrderDTO>>> {
//        val email = requestHttp.getAttribute("email") as String
        return permissionMiddleware.enforce(email, "CAN_SEARCH_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                orderService.searchOrders(email, statuses, dateFrom, dateTo).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { orders ->
                        ResponseEntity.ok(
                            ApiResponse(
                                success = true,
                                data =
                                    orders.map {
                                        it.toDTO()
                                    },
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
        @RequestParam email: String,
        @RequestBody request: PlaceOrderRequestDTO,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<OrderDTO>> {
//        val email = requestHttp.getAttribute("email") as String
        return permissionMiddleware.enforce(email, "CAN_PLACE_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                orderService.createOrder(email, request).fold(
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
        @RequestParam email: String,
        @PathVariable orderId: Long,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<OrderDTO>> {
//        val email = requestHttp.getAttribute("email") as String
        return permissionMiddleware.enforce(email, "CAN_CANCEL_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                orderService.cancelOrder(orderId, email).fold(
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
        @RequestParam email: String,
        @PathVariable orderId: Long,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<String>> {
//        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(email, "CAN_TRACK_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                orderService.trackOrder(orderId, email).fold(
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
        @RequestParam email: String,
        @RequestBody request: PlaceOrderRequestDTO,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<String>> {
//        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(email, "CAN_SCHEDULE_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                scheduledOrderService
                    .scheduleOrder(
                        email,
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
