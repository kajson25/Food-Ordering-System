package paket.backend.dish

import arrow.core.Either
import org.springframework.stereotype.Service
import paket.backend.arrow.AppError
import paket.backend.database.Dish

@Service
class DishService(
    private val dishRepository: DishRepository,
) {
    fun allDishes(email: String): Either<AppError, List<Dish>> = Either.Right(dishRepository.findAll())
}
