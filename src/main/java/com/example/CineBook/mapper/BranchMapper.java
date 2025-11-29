package com.example.CineBook.mapper;

import com.example.CineBook.dto.branch.BranchRequest;
import com.example.CineBook.dto.branch.BranchResponse;
import com.example.CineBook.model.Branch;
import org.mapstruct.*;

import java.util.Map;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BranchMapper extends BaseMapper<BranchRequest, Branch> {
    
    BranchResponse toResponse(Branch branch);
    BranchResponse toResponse(Branch branch, @Context Map<Object, Object> context);
    
    Branch toEntity(BranchRequest request);
    
    void updateEntity(BranchRequest request, @MappingTarget Branch branch);
}
