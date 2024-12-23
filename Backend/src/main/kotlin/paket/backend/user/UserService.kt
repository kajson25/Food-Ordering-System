package paket.backend.user

import arrow.core.Either
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.database.PasswordUtil
import paket.backend.database.User
import paket.backend.dtos.UserRequestDto

@Service
class UserService(
    private val userRepository: UserRepository,
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
        return Either.Right(userRepository.save(user))
    }

    fun getUserById(id: Long): Either<AppError, User> = userRepository.findById(id).let { Either.Right(it.get()) }

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

        existingUser.firstName = userRequest.firstName
        existingUser.lastName = userRequest.lastName
        existingUser.password = PasswordUtil.hash(userRequest.password)
        existingUser.email = userRequest.email
        existingUser.isAdmin = userRequest.isAdmin

        return Either.Right(userRepository.save(existingUser))
    }

    fun deleteUser(email: String): Either<AppError, String> {
        val user =
            userRepository.findByEmail(email)
                ?: return Either.Left(AppError.NotFound("User", 0))

        userRepository.delete(user)
        return Either.Right("User with email $email has been deleted.")
    }
}
