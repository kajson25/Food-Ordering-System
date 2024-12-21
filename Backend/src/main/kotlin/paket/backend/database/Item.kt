@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.database

import jakarta.persistence.*

@Entity
@Table(name = "items", uniqueConstraints = [UniqueConstraint(columnNames = ["order_id", "dish_id"])])
data class Item(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    var dish: Dish? = null,
    @Column(nullable = false)
    var quantity: Int = 1,
)
