@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.database

import jakarta.persistence.*

@Entity
@Table(name = "dishes")
data class Dish(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    var name: String = "",
    @Column
    var description: String? = null,
    @Column(nullable = false)
    var price: Double = 0.0,
)
