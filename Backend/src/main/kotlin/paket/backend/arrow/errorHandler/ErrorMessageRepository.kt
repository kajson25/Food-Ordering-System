package paket.backend.arrow.errorHandler

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import paket.backend.database.ErrorMessage

@Repository
interface ErrorMessageRepository : JpaRepository<ErrorMessage, Long> {
    @Query(
        """
        SELECT e FROM ErrorMessage e
        JOIN Order o ON e.orderId = o.id
        JOIN User u ON o.createdBy.id = u.id
        WHERE u.email = :email
        """,
    )
    fun findAllByUserEmail(
        @Param("email") email: String,
        pageable: Pageable,
    ): Page<ErrorMessage>
}
