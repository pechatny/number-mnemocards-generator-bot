package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.adapter;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.TokenTransactionRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataTokenTransactionRepository;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper.TokenTransactionPersistenceMapper;
import org.springframework.stereotype.Component;

@Component
public class TokenTransactionRepositoryAdapter implements TokenTransactionRepositoryPort {
    private final SpringDataTokenTransactionRepository repository;
    private final TokenTransactionPersistenceMapper mapper;

    public TokenTransactionRepositoryAdapter(SpringDataTokenTransactionRepository repository, TokenTransactionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public TokenTransaction save(TokenTransaction tokenTransaction) {
        var entity = repository.save(
            mapper.toEntity(tokenTransaction)
        );

        return mapper.toDomain(entity);
    }
}
