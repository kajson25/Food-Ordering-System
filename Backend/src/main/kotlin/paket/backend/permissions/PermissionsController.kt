@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.permissions

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paket.backend.arrow.ApiResponse
import paket.backend.dtos.PermissionResponseDto
import paket.backend.dtos.toPermissionResponseDto
import paket.backend.user.UserService

@RestController
@RequestMapping("/permissions")
class PermissionsController(
    private val permissionService: PermissionService,
    private val userService: UserService,
) {
    // Get all permissions for a specific user
    @GetMapping("/{email}")
    fun getPermissionsForUser(
        @PathVariable email: String,
    ): ResponseEntity<ApiResponse<List<PermissionResponseDto>>> =
        userService.getUserByEmail(email).fold(
            ifLeft = { error ->
                ResponseEntity.badRequest().body(ApiResponse(success = false, error = error.message))
            },
            ifRight = { user ->
                val permissions = permissionService.getUserPermissions(user.email)
                ResponseEntity.ok(ApiResponse(success = true, data = permissions.map { it.toPermissionResponseDto() }))
            },
        )

    // Get all available permissions in the system
    @GetMapping("/all")
    fun getAllPermissions(): ResponseEntity<ApiResponse<List<PermissionResponseDto>>> {
        val permissions = permissionService.getAllPermissions()
        return ResponseEntity.ok(
            ApiResponse(success = true, data = permissions.map { it.toPermissionResponseDto() }),
        )
    }
}
