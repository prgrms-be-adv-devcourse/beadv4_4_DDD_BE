package com.modeunsa.boundedcontext.auth.out.repository;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthAccessTokenBlacklist;
import org.springframework.data.repository.CrudRepository;

public interface AuthAccessTokenBlacklistRepository
    extends CrudRepository<AuthAccessTokenBlacklist, String> {}
