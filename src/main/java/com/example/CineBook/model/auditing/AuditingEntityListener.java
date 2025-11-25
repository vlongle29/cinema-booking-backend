package com.example.CineBook.model.auditing;

import com.example.CineBook.common.security.SecurityUtils;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;
import java.util.UUID;

public class AuditingEntityListener {

    // UUID cố định cho anonymous user
    private static final UUID ANONYMOUS_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @PrePersist
    public void prePersist(AuditingEntity entity) {
        Instant now = Instant.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        // Lấy user ID hiện tại, nếu không có thì dùng anonymous
        UUID currentUserId = getCurrentUserIdOrAnonymous();
        entity.setCreateBy(currentUserId);
        entity.setUpdateBy(currentUserId);
    }

    @PreUpdate
    public void preUpdate(AuditingEntity entity) {
        entity.setUpdateTime(Instant.now());

        // Lấy user ID hiện tại, nếu không có thì dùng anonymous
        UUID currentUserId = getCurrentUserIdOrAnonymous();
        entity.setUpdateBy(currentUserId);

        if (Boolean.TRUE.equals(entity.getIsDelete())) {
            entity.setDeleteTime(Instant.now());
            entity.setDeleteBy(currentUserId);
        }
    }

    /**
     * Lấy user ID hiện tại, nếu không có thì trả về anonymous user ID
     * @return UUID của user hiện tại hoặc anonymous user ID
     */
    private UUID getCurrentUserIdOrAnonymous() {
        try {
            Object userLogin = SecurityUtils.getCurrentUserLogin();
            if (userLogin == null) {
                return ANONYMOUS_USER_ID;
            }

            String userIdString = String.valueOf(userLogin);

            // Kiểm tra nếu là "null" string hoặc "Optional.empty"
            if ("null".equals(userIdString) || "Optional.empty".equals(userIdString)) {
                return ANONYMOUS_USER_ID;
            }

            // Thử parse thành UUID
            return UUID.fromString(userIdString);
        } catch (Exception e) {
            // Nếu có lỗi gì, trả về anonymous user ID
            return ANONYMOUS_USER_ID;
        }
    }
}
