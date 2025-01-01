@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.security.jwt

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paket.backend.arrow.ApiResponse
import paket.backend.dtos.LoginData
import paket.backend.permissions.PermissionService
import paket.backend.user.UserService

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val jwtTokenUtil: JwtTokenUtil,
    private val permissionService: PermissionService,
) {
    @PostMapping("/login")
    fun login(
        @RequestBody data: LoginData,
    ): ResponseEntity<ApiResponse<String>> =
        userService.authenticate(data.email, data.password).fold(
            ifLeft = { error -> ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message)) },
            ifRight = { user ->
                val permissions = permissionService.getUserPermissions(data.email).map { it.name }
                val token = jwtTokenUtil.generateToken(user.email, permissions)
                ResponseEntity.ok(ApiResponse(success = true, data = token))
            },
        )
}
