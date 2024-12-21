@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.database

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "error_messages")
data class ErrorMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    var date: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = true)
    var orderId: Long? = null,
    @Column(nullable = false)
    var operation: String = "",
    @Column(nullable = false)
    var message: String = "",
)
