package paket.backend.arrow.errorHandler

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import paket.backend.database.ErrorMessage
import paket.backend.dtos.ErrorMessageDTO
import paket.backend.dtos.toDTO
import paket.backend.user.UserService
import java.time.LocalDateTime

@Service
class ErrorMessageService(
    private val errorMessageRepository: ErrorMessageRepository,
    private val userService: UserService,
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

    fun getErrors(
        email: String,
        page: Int,
        size: Int,
    ): Page<ErrorMessageDTO> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"))
        val user = userService.getUserByEmail(email).getOrNull()
        return if (user!!.isAdmin) {
            errorMessageRepository.findAll(pageable).map { it.toDTO() }
        } else {
            errorMessageRepository.findAllByUserEmail(email, pageable).map { it.toDTO() }
        }
    }
}
