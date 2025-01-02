@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.arrow

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paket.backend.database.ErrorMessage
import paket.backend.permissions.PermissionMiddleware
import paket.backend.security.jwt.JwtTokenUtil

@RestController
class ErrorController(
    private val errorMessageService: ErrorMessageService,
    private val permissionMiddleware: PermissionMiddleware,
    private val jwtTokenUtil: JwtTokenUtil,
) {
    @GetMapping("/errors")
    fun getErrorMessages(
        @RequestParam page: Int,
        @RequestParam size: Int,
        requestHttp: HttpServletRequest,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<List<ErrorMessage>>> {
        val email = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        // todo - NE
        return permissionMiddleware.enforce(email, "CAN_VIEW_ERRORS").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                val errors = errorMessageService.getErrors()
                ResponseEntity.ok(ApiResponse(success = true, data = errors))
            },
        )
    }
}
