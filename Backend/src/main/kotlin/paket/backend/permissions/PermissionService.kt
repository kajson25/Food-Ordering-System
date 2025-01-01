package paket.backend.permissions

import arrow.core.Either
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.database.Permission
import paket.backend.database.UserPermission
import paket.backend.user.UserRepository

@Service
class PermissionService(
    private val permissionRepository: PermissionRepository,
    private val userPermissionRepository: UserPermissionRepository,
    private val userRepository: UserRepository,
) {
    fun assignPermissionToUser(
        email: String,
        permissionName: String,
    ): Either<AppError, UserPermission> {
        val permission =
            permissionRepository.findByName(permissionName)
                ?: return Either.Left(AppError.NotFound("Permission", 0))
        println("Permission to add: $permissionName")

        val user = userRepository.findByEmail(email)
        val userPermission = UserPermission(user = user, permission = permission)
        return Either.Right(userPermissionRepository.save(userPermission))
    }

    fun getUserPermissions(email: String): List<Permission> = userPermissionRepository.findAllByUserEmail(email).map { it.permission!! }

    fun hasPermission(
        email: String,
        permissionName: String,
    ): Boolean {
        val userPermissions = getUserPermissions(email)
        return userPermissions.any { it.name == permissionName }
    }

    fun getAllPermissions(): List<Permission> = permissionRepository.findAll()

    fun removePermissionFromUser(
        email: String,
        permissionName: String,
    ): Either<AppError, Unit> {
        val permission =
            permissionRepository.findByName(permissionName)
                ?: return Either.Left(AppError.NotFound("Permission", 0))

        val user = userRepository.findByEmail(email)

        val userPermission =
            userPermissionRepository.findByUserIdAndPermissionId(user!!.id, permission.id)
                ?: return Either.Left(AppError.NotFound("Permission for user", 0))

        userPermissionRepository.delete(userPermission)
        return Either.Right(Unit)
    }
}
