package com.example.CineBook.common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Lắng nghe các event xóa cache user và thực hiện xóa cache tương ứng.
 */
@Component
public class UserCacheEventListener {

    private final ApplicationContext applicationContext;

    // inject proxy của chính bean
    public UserCacheEventListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Khi nhận được event, tiến hành xóa cache userInfo, roles, permissions, menus cho các userId liên quan.
     * @param event Event chứa danh sách userId cần xóa cache
     */
    @EventListener
    public void handle(UserCacheEvictEvent event) {
        List<UUID> userIds = event.getUserIds();
        if (userIds == null || userIds.isEmpty()) return;

        UserCacheEventListener self = applicationContext.getBean(UserCacheEventListener.class);
        for (UUID userId : userIds) {
           self.evictAllUserCaches(userId);  // gọi qua proxy → chắc chắn xóa cache
        }
    }

    /**
     * Evicts all caches related to a specific user.
     * The @Caching annotation groups multiple cache operations.
     * @param userId The ID of the user whose caches need to be evicted.
     */
    @Caching(evict = {
            @CacheEvict(value = "userInfo", key = "#userId"),
            @CacheEvict(value = "roles", key = "#userId"),
            @CacheEvict(value = "permissions", key = "#userId"),
            @CacheEvict(value = "menus", key = "#userId")
    })
    public void evictAllUserCaches(UUID userId) {
        // This method's body can be empty.
        // The annotations handle the cache eviction logic.
    }

}


/**
 * Nhờ có @EventListener thì khi mình gọi eventPublisher.publishEvent(new UserCacheEvictEvent(affectedUserIds));
 * -> Spring sẽ tìm tất cả các listener mà method có @EventListener nhân UserCacheEvictEvent
 */
