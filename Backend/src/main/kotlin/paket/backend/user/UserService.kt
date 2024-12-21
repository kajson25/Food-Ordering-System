package paket.backend.user

import arrow.core.Either
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.database.User

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun findById(id: Long): Either<AppError, User> {
        val user = userRepository.findById(id)
        return if (user.isPresent) {
            Either.Right(user.get())
        } else {
            Either.Left(AppError.NotFound("User", id))
        }
    }
}
