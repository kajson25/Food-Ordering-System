@file:Suppress("ktlint:standard:no-wildcard-imports")

package paket.backend.database

import jakarta.persistence.*

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.ORDERED,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: User? = null,
    @Column(nullable = false)
    var active: Boolean = true,
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<Item> = mutableListOf(),
)

enum class OrderStatus {
    ORDERED,
    PREPARING,
    IN_DELIVERY,
    DELIVERED,
    CANCELED,
}
