@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.database

import jakarta.persistence.*

@Entity
@Table(name = "permissions")
data class Permissions(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true)
    val name: String = "",
)
