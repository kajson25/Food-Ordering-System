package paket.backend.permissions

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import paket.backend.database.UserPermission

@Repository
interface UserPermissionRepository : JpaRepository<UserPermission, Long> {
    fun findAllByUserEmail(email: String): List<UserPermission>

    fun findByUserIdAndPermissionId(
        userId: Long,
        permissionId: Long,
    ): UserPermission?
}
