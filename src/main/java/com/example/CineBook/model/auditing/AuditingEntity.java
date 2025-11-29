package com.example.CineBook.model.auditing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Data
@AllArgsConstructor
public abstract class AuditingEntity implements SoftDeletable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private Instant createTime;
    private UUID createBy;
    private Instant updateTime;
    private UUID updateBy;
    private Boolean isDelete = false;
    private Instant deleteTime;
    private UUID deleteBy;

    @Override
    public void setDeleted(Boolean deleted) {
        this.isDelete = deleted;
    }

    @Override
    public Boolean getIsDelete() {
        return this.isDelete;
    }

}

/// Kiến thức học được
/**
 * @MappedSuperclass dùng để khai báo rằng class này không phải là entity, nhưng các entity con kế thừa
 * nó sẽ thừa hưởng toàn bộ field và mapping.
 * Nói đơn giản:
 *
 * Class cha không tạo bảng trong DB.
 * Các class con có @Entity sẽ nhận toàn bộ thuộc tính + mapping của class cha.
 * Thường dùng để tạo base entity, như createTime, updateTime, id...
 *
 * @EntityListeners(AuditingEntityListener.class)
 * Annotation này nói với JPA rằng:
 * --> “Hãy sử dụng class AuditingEntityListener để lắng nghe các sự kiện lifecycle như:
 * @PrePersist, @PostPersist, @PreUpdate, @PreRemove, ...”
 * Nó cho phép bạn tự động:
 * Gắn createTime khi entity được insert
 * Gắn updateTime khi entity được update
 * Tự động set createBy, updateBy
 * Tự xử lý khi entity bị soft-delete
 *
 */
