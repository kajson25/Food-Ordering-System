package paket.backend.arrow

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import paket.backend.database.ErrorMessage

@Repository
interface ErrorMessageRepository : JpaRepository<ErrorMessage, Long>
