package paket.backend.dtos

import paket.backend.database.User

data class UserDTO(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val isAdmin: Boolean,
)

fun User.toDTO() =
    UserDTO(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        isAdmin = this.isAdmin,
    )
