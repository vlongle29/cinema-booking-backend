package com.example.CineBook.common.service;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.UUID;

/**
 * Event dùng để thông báo cần xóa cache liên quan đến user.
 * Có thể truyền 1 userId hoặc danh sách userId.
 */
public class UserCacheEvictEvent {
    private final List<UUID> userIds;

    /**
     * Khởi tạo event với danh sách userId cần xóa cache.
     * @param userIds Danh sách userId cần xóa cache
     */
    public UserCacheEvictEvent(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public List<UUID> getUserIds() { return userIds; }
}
