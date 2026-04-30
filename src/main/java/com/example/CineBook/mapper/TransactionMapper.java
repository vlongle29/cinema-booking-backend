package com.example.CineBook.mapper;

import com.example.CineBook.dto.transaction.TransactionResponse;
import com.example.CineBook.model.Transaction;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    
    TransactionResponse toResponse(Transaction transaction);
}
