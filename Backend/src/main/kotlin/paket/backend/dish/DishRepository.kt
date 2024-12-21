package paket.backend.dish

import org.springframework.data.jpa.repository.JpaRepository
import paket.backend.database.Dish

interface DishRepository : JpaRepository<Dish, Long>
