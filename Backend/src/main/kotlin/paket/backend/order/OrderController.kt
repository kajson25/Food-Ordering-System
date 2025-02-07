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
import paket.backend.security.jwt.JwtTokenUtil
import java.time.LocalDateTime

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
    private val scheduledOrderService: ScheduledOrderService,
    private val permissionMiddleware: PermissionMiddleware,
    private val jwtTokenUtil: JwtTokenUtil,
) {
    @GetMapping("/all")
    fun allOrders(
        requestHttp: HttpServletRequest,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<List<OrderDTO>>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        return orderService.allOrders(email).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { orders -> ResponseEntity.ok(ApiResponse(success = true, data = orders.map { it.toDTO() })) },
        )
    }

    @GetMapping("/search")
    fun searchOrders(
        @RequestParam statuses: List<OrderStatus>?,
        @RequestParam dateFrom: String?,
        @RequestParam dateTo: String?,
        @RequestParam userId: Long?,
        requestHttp: HttpServletRequest,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<List<OrderDTO>>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        statuses!!.forEach { println("Status: $it") }
        println("DateFrom: $dateFrom")
        println("DateTo: $dateTo")
        return permissionMiddleware.enforce(email, "CAN_SEARCH_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                // todo fix user id
                orderService.searchOrders(email, statuses, dateFrom, dateTo, userId).fold(
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
        @RequestBody request: PlaceOrderRequestDTO,
        requestHttp: HttpServletRequest,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<OrderDTO>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
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
        @PathVariable orderId: Long,
        requestHttp: HttpServletRequest,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<OrderDTO>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
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
        @PathVariable orderId: Long,
        requestHttp: HttpServletRequest,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<String>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
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

    @GetMapping("/recent")
    fun recentOrder(
        requestHttp: HttpServletRequest,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<OrderDTO>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        return permissionMiddleware.enforce(email, "CAN_SEARCH_ORDER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                orderService.recentOrder(email).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { order -> ResponseEntity.ok(ApiResponse(success = true, data = order.toDTO())) },
                )
            },
        )
    }

    // works - no-permission, schedule before today
    @PostMapping("/schedule")
    fun scheduleOrder(
        @RequestBody request: PlaceOrderRequestDTO,
        requestHttp: HttpServletRequest,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<String>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
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
