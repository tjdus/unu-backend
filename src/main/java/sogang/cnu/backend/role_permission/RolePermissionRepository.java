package sogang.cnu.backend.role_permission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
    List<RolePermission> findByRoleNameIn(List<String> roleNames);
}
