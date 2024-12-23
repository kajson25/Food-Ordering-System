package paket.backend.dtos

import paket.backend.database.User

data class UserRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val isAdmin: Boolean,
)

data class UserResponseDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val isAdmin: Boolean,
)

fun User.toDTO() =
    UserResponseDto(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        isAdmin = this.isAdmin,
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
