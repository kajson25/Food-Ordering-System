package paket.backend.arrow

import org.springframework.stereotype.Service
import paket.backend.database.ErrorMessage
import java.time.LocalDateTime

@Service
class ErrorMessageService(
    private val errorMessageRepository: ErrorMessageRepository,
) {
    fun logError(
        orderId: Long?,
        operation: String,
        message: String,
    ): ErrorMessage {
        val errorMessage =
            ErrorMessage(
                date = LocalDateTime.now(),
                orderId = orderId,
                operation = operation,
                message = message,
            )
        return errorMessageRepository.save(errorMessage)
    }
}
