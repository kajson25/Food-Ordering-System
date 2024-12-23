package paket.backend.permissions

import arrow.core.Either
import org.springframework.stereotype.Component
import paket.backend.arrow.AppError

@Component
class PermissionMiddleware(
    private val permissionService: PermissionService,
) {
    fun enforce(
        userId: Long,
        permissionName: String,
    ): Either<AppError, Unit> =
        if (permissionService.hasPermission(userId, permissionName)) {
            Either.Right(Unit)
        } else {
            Either.Left(AppError.Unauthorized("Permission $permissionName required."))
        }
}
