@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.user

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paket.backend.arrow.ApiResponse
import paket.backend.dtos.UserRequestDto
import paket.backend.dtos.UserResponseDto
import paket.backend.dtos.toDTO
import paket.backend.permissions.PermissionMiddleware
import paket.backend.security.jwt.JwtTokenUtil

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val permissionMiddleware: PermissionMiddleware,
    private val jwtTokenUtil: JwtTokenUtil,
) {
    @GetMapping("/{email}")
    fun getUserByEmail(
        @PathVariable email: String,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val loggedInEmail = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        return permissionMiddleware.enforce(loggedInEmail, "CAN_READ_USER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                userService.getUserByEmail(email).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { user -> ResponseEntity.ok(ApiResponse(success = true, data = user.toDTO())) },
                )
            },
        )
    }

    @GetMapping("/all")
    fun getAllUsers(
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<ApiResponse<List<UserResponseDto>>> {
        val loggedInEmail = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        return permissionMiddleware.enforce(loggedInEmail, "CAN_READ_USER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                userService.getAllUsers().fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { users ->
                        ResponseEntity.ok(
                            ApiResponse(
                                success = true,
                                data = users.map { it.toDTO() },
                            ),
                        )
                    },
                )
            },
        )
    }

    @PostMapping("/create")
    fun createUser(
        @RequestBody userRequest: UserRequestDto,
        @RequestHeader("Authorization") token: String, // Extract JWT from the header
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        println("user request: $userRequest")
        val loggedInEmail = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        return permissionMiddleware.enforce(loggedInEmail, "CAN_CREATE_USER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                userService.createUser(userRequest).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { user -> ResponseEntity.ok(ApiResponse(success = true, data = user.toDTO())) },
                )
            },
        )
    }

    @PutMapping("/update/{email}")
    fun updateUser(
        @PathVariable email: String,
        @RequestBody userRequest: UserRequestDto,
        @RequestHeader("Authorization") token: String, // Extract JWT from the header
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val loggedInEmail = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        return permissionMiddleware.enforce(loggedInEmail, "CAN_UPDATE_USER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                userService.updateUser(email, userRequest).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { user -> ResponseEntity.ok(ApiResponse(success = true, data = user.toDTO())) },
                )
            },
        )
    }

    @DeleteMapping("/delete/{email}")
    fun deleteUser(
        @PathVariable email: String,
        @RequestHeader("Authorization") token: String, // Extract JWT from the header
    ): ResponseEntity<ApiResponse<String>> {
        val loggedInEmail = jwtTokenUtil.extractClaims(token.substringAfter("Bearer ")).subject
        return permissionMiddleware.enforce(loggedInEmail, "CAN_DELETE_USER").fold(
            ifLeft = { error -> ResponseEntity.ok(ApiResponse(success = false, error = error.message)) },
            ifRight = {
                userService.deleteUser(email).fold(
                    ifLeft = { error ->
                        ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
                    },
                    ifRight = { message -> ResponseEntity.ok(ApiResponse(success = true, data = message)) },
                )
            },
        )
    }
}
