@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.database

import jakarta.persistence.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(columnNames = ["email"])])
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    var firstName: String = "",
    @Column(nullable = false)
    var lastName: String = "",
    @Column(nullable = false, unique = true)
    var email: String = "",
    @Column(nullable = false)
    var password: String = "",
    @Column(nullable = false)
    var isAdmin: Boolean = false,
) {
    override fun toString(): String = "User(id=$id, firstName=$firstName, lastName=$lastName, email=$email)"
}

object PasswordUtil {
    private val encoder = BCryptPasswordEncoder()

    fun hash(password: String): String = encoder.encode(password)

    fun matches(
        raw: String,
        hashed: String,
    ): Boolean = encoder.matches(raw, hashed)
}
