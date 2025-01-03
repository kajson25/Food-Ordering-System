package paket.backend.dish

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import paket.backend.arrow.ApiResponse
import paket.backend.dtos.DishDTO
import paket.backend.dtos.toDTO
import paket.backend.security.jwt.JwtTokenUtil

@RestController
@RequestMapping("/dishes")
class DishController(
    private val jwtTokenUtil: JwtTokenUtil,
    private val dishService: DishService,
) {
    @GetMapping("/all")
    fun allOrders(
        requestHttp: HttpServletRequest,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<List<DishDTO>>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        return dishService.allDishes(email).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { orders -> ResponseEntity.ok(ApiResponse(success = true, data = orders.map { it.toDTO() })) },
        )
    }
}
