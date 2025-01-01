package paket.backend.permissions

import arrow.core.Either
import org.springframework.stereotype.Component
import paket.backend.arrow.AppError
import paket.backend.user.UserService

@Component
class PermissionMiddleware(
    private val permissionService: PermissionService,
    private val userService: UserService,
) {
    fun enforce(
        loggedInEmail: String,
        permissionName: String,
    ): Either<AppError, Unit> {
        val user =
            userService.getUserByEmail(loggedInEmail).getOrNull()
                ?: return Either.Left(AppError.NotFound("User", 0))

        return if (permissionService.hasPermission(user.email, permissionName)) {
            Either.Right(Unit)
        } else {
            Either.Left(AppError.Unauthorized("Permission $permissionName required."))
        }
    }
}
