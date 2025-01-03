package paket.backend.dtos

import org.springframework.data.domain.Page
import paket.backend.database.ErrorMessage
import java.time.LocalDateTime

data class ErrorMessageDTO(
    val date: LocalDateTime,
    val orderId: Long,
    val operation: String,
    val message: String,
)

data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
)

fun ErrorMessage.toDTO(): ErrorMessageDTO {
    if (this.orderId == null) {
        this.orderId = 1
    }
    return ErrorMessageDTO(
        date = this.date,
        orderId = this.orderId!!,
        operation = this.operation,
        message = this.message,
    )
}

fun <T> Page<T>.toPaginatedResponse(): PaginatedResponse<T> =
    PaginatedResponse(
        content = this.content,
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        currentPage = this.number,
        pageSize = this.size,
    )
