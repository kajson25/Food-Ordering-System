package paket.backend.dtos

import paket.backend.database.Permission
import paket.backend.database.User

data class UserRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val isAdmin: Boolean,
    val permissions: List<String>,
)

data class UserResponseDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val isAdmin: Boolean,
    val permissions: List<String>,
)

data class PermissionRequestDto(
    val email: String,
    val permission: String,
)

data class PermissionResponseDto(
    val permission: String,
)

fun User.toDTO() =
    UserResponseDto(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        isAdmin = this.isAdmin,
        permissions = listOf(),
    )

// Extension to map a UserRequestDto to a User entity
fun UserRequestDto.toEntity(hashedPassword: String): User =
    User(
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        password = hashedPassword,
        isAdmin = this.isAdmin,
    )

fun Permission.toPermissionResponseDto(): PermissionResponseDto = PermissionResponseDto(permission = this.name)

fun PermissionRequestDto.toPermission(): Permission =
    Permission(
        name = this.permission,
    )
