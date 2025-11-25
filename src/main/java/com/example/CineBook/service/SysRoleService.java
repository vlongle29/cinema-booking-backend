package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.sysPermission.SysPermissionResponse;
import com.example.CineBook.dto.sysRole.*;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing system roles ({@link com.metasol.cms.model.SysRole}).
 */
public interface SysRoleService {
    /**
     * Creates a new system role.
     */
    SysRoleResponse create(SysRoleRequest request);

    /**
     * Updates an existing system role.
     */
    SysRoleResponse update(SysRoleRequest request);

    /**
     * Deletes a system role by its ID (soft delete).
     *
     * @param id The UUID of the role to delete.
     */
    void delete(UUID id);
    void deleteRoles(List<UUID> roleIds);

    /**
     * Assigns a list of permissions to a role.
     * This will overwrite all existing permissions for the role.
     *
     * @param request The request containing the role ID and a list of permission IDs.
     */
    void assignPermissionsToRole(AssignPermissionsRequest request);

    /**
     * Retrieves all permissions assigned to a specific role.
     *
     * @param roleId The UUID of the role.
     * @return A list of permissions.
     */
    List<SysPermissionResponse> getPermissionsByRoleId(UUID roleId);

    /**
     * Finds a system role by its ID.
     *
     * @param id The UUID of the role.
     * @return An Optional containing the role response DTO if found, otherwise empty.
     */
    SysRoleResponse findById(UUID id);

    /**
     * Retrieves all system roles that are not deleted.
     *
     * @return A list of all active role response DTOs.
     */
    List<SysRoleResponse> findAll();

    /**
     * Searches for system roles with pagination and filtering.
     *
     * @param searchDTO The search criteria and pagination information.
     * @return A Page of role response DTOs.
     */
    PageResponse<SysRoleResponse> search(SysRoleSearchDTO searchDTO);

    List<SysRoleResponse> getRoleInfoByUserId(UUID userId);
    void assignRoleToUsers(UUID roleId, AssignRoleToUserRequest request);
}
