package paket.backend.user

import arrow.core.Either
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.database.PasswordUtil
import paket.backend.database.User
import paket.backend.database.UserPermission
import paket.backend.dtos.UserRequestDto
import paket.backend.permissions.PermissionRepository
import paket.backend.permissions.PermissionService
import paket.backend.permissions.UserPermissionRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val permissionRepository: PermissionRepository,
    private val userPermissionRepository: UserPermissionRepository,
    private val permissionService: PermissionService,
) {
    fun authenticate(
        email: String,
        password: String,
    ): Either<AppError, User> {
        val user =
            userRepository.findByEmail(email)
                ?: return Either.Left(AppError.NotFound("User", 0))

        if (!PasswordUtil.matches(password, user.password)) {
            return Either.Left(AppError.ValidationFailed("Invalid password"))
        }

        return Either.Right(user)
    }

    fun createUser(userRequest: UserRequestDto): Either<AppError, User> {
        if (userRepository.findByEmail(userRequest.email) != null) {
            return Either.Left(AppError.ValidationFailed("User with email ${userRequest.email} already exists."))
        }

        val hashedPassword = PasswordUtil.hash(userRequest.password)

        val user =
            User(
                firstName = userRequest.firstName,
                lastName = userRequest.lastName,
                email = userRequest.email,
                password = hashedPassword,
                isAdmin = userRequest.isAdmin,
            )
        userRepository.save(user)

        userRequest.permissions.forEach {
            permissionService.assignPermissionToUser(user.email, it)
        }
        return Either.Right(user)
    }

    fun getUserByEmail(email: String): Either<AppError, User> =
        userRepository.findByEmail(email)?.let { Either.Right(it) }
            ?: Either.Left(AppError.NotFound("User", 0))

    fun getAllUsers(): Either<AppError, List<User>> = Either.Right(userRepository.findAll())

    fun updateUser(
        email: String,
        userRequest: UserRequestDto,
    ): Either<AppError, User> {
        val existingUser =
            userRepository.findByEmail(email)
                ?: return Either.Left(AppError.NotFound("User", 0))

        // Update user details
        existingUser.firstName = userRequest.firstName
        existingUser.lastName = userRequest.lastName
        existingUser.password = PasswordUtil.hash(userRequest.password)
        existingUser.email = userRequest.email
        existingUser.isAdmin = userRequest.isAdmin

        // Save user details
        val updatedUser = userRepository.save(existingUser)

        // Update permissions
        val permissionsUpdateResult = updateUserPermissions(updatedUser, userRequest.permissions)
        if (permissionsUpdateResult is Either.Left) {
            return Either.Left(permissionsUpdateResult.value)
        }

        return Either.Right(updatedUser)
    }

    private fun updateUserPermissions(
        user: User,
        requestedPermissions: List<String>,
    ): Either<AppError, Unit> {
        requestedPermissions.forEach { println("Reqeust: $it") }
        val currentPermissions = userPermissionRepository.findAllByUserEmail(user.email)
        val currentPermissionNames = currentPermissions.map { it.permission!!.name }

        // Permissions to add
        val permissionsToAdd = requestedPermissions.filter { it !in currentPermissionNames }
        for (permissionName in permissionsToAdd) {
            val permission =
                permissionRepository.findByName(permissionName)
                    ?: return Either.Left(AppError.NotFound("Permission $permissionName not found.", user.id))
            userPermissionRepository.save(UserPermission(user = user, permission = permission))
        }

        // Permissions to remove
        val permissionsToRemove = currentPermissionNames.filter { it !in requestedPermissions }
        for (permissionName in permissionsToRemove) {
            val permission =
                permissionRepository.findByName(permissionName)
                    ?: continue
            val userPermission =
                userPermissionRepository.findByUserIdAndPermissionId(user.id, permission.id)
                    ?: continue
            userPermissionRepository.delete(userPermission)
        }

        return Either.Right(Unit)
    }

    fun deleteUser(email: String): Either<AppError, String> {
        val user =
            userRepository.findByEmail(email)
                ?: return Either.Left(AppError.NotFound("User", 0))

        userRepository.delete(user)
        return Either.Right("User with email $email has been deleted.")
    }
}
