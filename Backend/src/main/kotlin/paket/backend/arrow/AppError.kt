package paket.backend.arrow

sealed class AppError(
    val message: String,
) {
    data class NotFound(
        val entity: String,
        val id: Long,
    ) : AppError("$entity with ID $id not found.")

    data class ValidationFailed(
        val reason: String,
    ) : AppError(reason)

    data class Unauthorized(
        val action: String,
    ) : AppError("Unauthorized to perform action: $action.")

    data class Unexpected(
        val details: String,
    ) : AppError("Unexpected error occurred: $details.")
}
