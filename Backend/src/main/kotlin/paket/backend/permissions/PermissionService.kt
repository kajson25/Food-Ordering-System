package paket.backend.permissions

import arrow.core.Either
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.database.Permission
import paket.backend.database.User
import paket.backend.database.UserPermission

@Service
class PermissionService(
    private val permissionRepository: PermissionRepository,
    private val userPermissionRepository: UserPermissionRepository,
) {
    fun assignPermissionToUser(
        userId: Long,
        permissionName: String,
    ): Either<AppError, UserPermission> {
        val permission =
            permissionRepository.findByName(permissionName)
                ?: return Either.Left(AppError.NotFound("Permission", 0))

        val userPermission = UserPermission(user = User(id = userId), permission = permission)
        return Either.Right(userPermissionRepository.save(userPermission))
    }

    fun getUserPermissions(userId: Long): List<Permission> = userPermissionRepository.findAllByUserId(userId).map { it.permission!! }

    fun hasPermission(
        userId: Long,
        permissionName: String,
    ): Boolean {
        val userPermissions = getUserPermissions(userId)
        return userPermissions.any { it.name == permissionName }
    }
}
