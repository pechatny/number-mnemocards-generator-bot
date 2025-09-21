package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.TokenBalanceEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SpringDataTokenBalanceRepository extends CrudRepository<TokenBalanceEntity, Long> {
    Optional<TokenBalanceEntity> findByUserId(Long userId);
}
