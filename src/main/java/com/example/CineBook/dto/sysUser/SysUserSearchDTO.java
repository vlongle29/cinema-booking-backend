package com.example.CineBook.dto.sysUser;

import com.example.CineBook.common.constant.LockFlag;
import com.example.CineBook.common.dto.request.SearchBaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserSearchDTO extends SearchBaseDto {
    @Schema(description = "Tìm kiếm theo tên đăng nhập của người dùng")
    private String username;

    @Schema(description = "Tìm kiếm theo địa chỉ email của người dùng")
    private String email;

    @Schema(description = "Tìm kiếm theo họ và tên của người dùng")
    private String name;

    @Schema(description = "Tìm kiếm theo số điện thoại của người dùng")
    private String phone;

    @Schema(description = "Lọc người dùng theo trạng thái khóa.", implementation = LockFlag.class, example = "LOCK")
    private LockFlag lockFlag;

    @Schema(description = "Lọc người dùng theo danh sách ID vai trò")
    private List<UUID> roleIds;

    @Schema(description = "Lọc người dùng theo danh sách ID")
    private List<UUID> ids;
}
