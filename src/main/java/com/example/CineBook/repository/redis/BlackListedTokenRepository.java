package com.example.CineBook.repository.redis;

import com.example.CineBook.model.BlacklistedToken;
import org.springframework.data.repository.CrudRepository;

public interface BlackListedTokenRepository extends CrudRepository<BlacklistedToken, String> {
    boolean existsByAccessToken(String jwt);
}
