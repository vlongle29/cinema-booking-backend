package com.example.CineBook.mapper;

import com.example.CineBook.dto.auth.RegisterRequest;
import com.example.CineBook.dto.sysRole.RoleInfo;
import com.example.CineBook.dto.sysUser.UserCreateRequest;
import com.example.CineBook.dto.sysUser.UserInfoResponse;
import com.example.CineBook.dto.sysUser.UserUpdateRequest;
import com.example.CineBook.model.SysUser;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    SysUser toEntity(RegisterRequest request);
    SysUser toEntity(UserCreateRequest request);
    UserInfoResponse toResponse(SysUser user);
    UserInfoResponse toResponse(SysUser user, @Context Map<UUID, List<RoleInfo>> rolesMap);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(UserUpdateRequest source, @MappingTarget SysUser target);

    UserInfoResponse toUserInfoResponse(SysUser user);

    default Page<UserInfoResponse> mapPageWithRoles(Page<SysUser> userPage, Map<UUID, List<RoleInfo>> rolesMap) {
        return userPage.map(user -> toResponse(user, rolesMap));
    }
}


/// <============= Kiến thức học được ===============>

/**
 * - componentModel = "spring": Cho phép Srping quản lý mapper này như một Bean
 * --> Nghĩa là bạn có thể @Autowired hoặc @Inject mapper trong service hoặc controller
 * <p>
 * <p>
 * - nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
 * --> Nghĩa là khi thuộc tính trong source = null → MapStruct sẽ bỏ qua (không ghi đè) giá trị của target.
 * <p>
 * <p>
 * - nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
 * --> Yêu cầu MapStruct luôn sinh thêm lệnh kiểm tra null trước khi gán giá trị
 * VD: target.setField(source.getField() != null ? source.getField() : target.getField());
 * --> Giúp tránh lỗi NullPointerException khi source có field null.
 * <p>
 * <p>
 * - unmappedTargetPolicy = ReportingPolicy.IGNORE
 * --> Dùng để tắt cảnh báo khi trong target có field mà source không có.
 * VD: public class UserDto {
 * private String name;
 * }
 * <p>
 * public class UserEntity {
 * private String name;
 * private LocalDate createdAt; // không có trong DTO
 * }
 * Cảnh báo: Unmapped target property: "createdAt" in target type "User".
 * -->N ếu bạn thêm ReportingPolicy.IGNORE, MapStruct sẽ bỏ qua cảnh báo đó, giúp code sạch hơn (nhất là khi DTO và Entity không trùng field 100%).
 */