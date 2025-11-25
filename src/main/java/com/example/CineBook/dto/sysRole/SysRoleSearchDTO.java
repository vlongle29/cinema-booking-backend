package com.example.CineBook.dto.sysRole;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysRoleSearchDTO extends SearchBaseDto {
    private String name;
    private String code;
}
