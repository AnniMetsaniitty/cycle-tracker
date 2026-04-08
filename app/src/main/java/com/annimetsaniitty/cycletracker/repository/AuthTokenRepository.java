package com.annimetsaniitty.cycletracker.repository;

import com.annimetsaniitty.cycletracker.model.AuthToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByTokenHash(String tokenHash);
}
