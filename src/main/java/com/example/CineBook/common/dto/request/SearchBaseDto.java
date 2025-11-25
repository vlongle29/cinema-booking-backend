package com.example.CineBook.common.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchBaseDto {
    /**
     * Số trang (bắt đầu từ 1, mặc định là 1).
     */
//    @Schema(description = "Số trang, bắt đầu từ 1", defaultValue = "1")
    private Integer page = 1;

    /**
     * Kích thước trang (số lượng bản ghi trên mỗi trang, mặc định là 10).
     */
//    @Schema(description = "Số lượng bản ghi trên mỗi trang", defaultValue = "10")
    private Integer size = 10;

    /**
     * Tên trường để sắp xếp. Mặc định là 'createTime'.
     */
//    @Schema(description = "Tên trường để sắp xếp. Ví dụ: 'username', 'email'", defaultValue = "createTime")
    private String sortBy = "createTime";

    /**
     * Từ khóa tìm kiếm chung, dùng để lọc kết quả trong các danh sách (ví dụ: dropdown).
     */
//    @Schema(description = "Từ khóa tìm kiếm chung")
    private String searchTerm;

    /**
     * Hướng sắp xếp ("ASC" hoặc "DESC"). Mặc định là 'DESC'.
     */
//    @Schema(description = "Hướng sắp xếp", defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";

}
