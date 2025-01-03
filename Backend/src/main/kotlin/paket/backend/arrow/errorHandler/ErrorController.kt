@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.arrow.errorHandler

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paket.backend.arrow.ApiResponse
import paket.backend.dtos.ErrorMessageDTO
import paket.backend.dtos.PaginatedResponse
import paket.backend.dtos.toPaginatedResponse
import paket.backend.security.jwt.JwtTokenUtil

@RestController
@RequestMapping("/errors")
class ErrorController(
    private val errorMessageService: ErrorMessageService,
    private val jwtTokenUtil: JwtTokenUtil,
) {
    @GetMapping
    fun getErrorMessages(
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<PaginatedResponse<ErrorMessageDTO>>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        val errors = errorMessageService.getErrors(email, page, size)
        return ResponseEntity.ok(
            ApiResponse(success = true, data = errors.toPaginatedResponse()),
        )
    }
}
