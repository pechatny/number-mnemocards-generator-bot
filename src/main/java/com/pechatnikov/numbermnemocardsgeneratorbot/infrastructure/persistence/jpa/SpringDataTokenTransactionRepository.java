package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.TokenTransactionEntity;
import org.springframework.data.repository.CrudRepository;

public interface SpringDataTokenTransactionRepository extends CrudRepository<TokenTransactionEntity, Long> {
}
