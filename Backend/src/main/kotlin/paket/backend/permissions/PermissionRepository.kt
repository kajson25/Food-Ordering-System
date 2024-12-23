package paket.backend.permissions

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import paket.backend.database.Permission

@Repository
interface PermissionRepository : JpaRepository<Permission, Long> {
    fun findByName(name: String): Permission?
}
