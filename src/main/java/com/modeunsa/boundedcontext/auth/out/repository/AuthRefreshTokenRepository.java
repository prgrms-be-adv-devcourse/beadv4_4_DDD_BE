package com.modeunsa.boundedcontext.auth.out.repository;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthRefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface AuthRefreshTokenRepository extends CrudRepository<AuthRefreshToken, Long> {}
