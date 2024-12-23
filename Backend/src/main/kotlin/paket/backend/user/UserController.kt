@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.user

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paket.backend.arrow.ApiResponse
import paket.backend.dtos.UserRequestDto
import paket.backend.dtos.UserResponseDto
import paket.backend.dtos.toDTO
import paket.backend.permissions.PermissionMiddleware

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val permissionMiddleware: PermissionMiddleware,
) {
    // works
    @GetMapping("/{email}")
    fun getUserByEmail(
        @PathVariable email: String,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(userId, "CAN_READ_USER").fold(
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
    fun getAllUsers(requestHttp: HttpServletRequest): ResponseEntity<ApiResponse<List<UserResponseDto>>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(userId, "CAN_READ_USER").fold(
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

    // works
    @PostMapping("/create")
    fun createUser(
        @RequestBody userRequest: UserRequestDto,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val userId = requestHttp.getAttribute("userId") as Long
        println("KONTORLER SUGAVI: $userId")
        return permissionMiddleware.enforce(userId, "CAN_CREATE_USER").fold(
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

    // works
    @PutMapping("/update/{email}")
    fun updateUser(
        @PathVariable email: String,
        @RequestBody userRequest: UserRequestDto,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(userId, "CAN_UPDATE_USER").fold(
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

    // works
    @DeleteMapping("/delete/{email}")
    fun deleteUser(
        @PathVariable email: String,
        requestHttp: HttpServletRequest,
    ): ResponseEntity<ApiResponse<String>> {
        val userId = requestHttp.getAttribute("userId") as Long
        return permissionMiddleware.enforce(userId, "CAN_DELETE_USER").fold(
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
