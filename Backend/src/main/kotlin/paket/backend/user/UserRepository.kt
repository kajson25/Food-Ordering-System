package paket.backend.user

import org.springframework.data.jpa.repository.JpaRepository
import paket.backend.database.User

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}
