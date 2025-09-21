package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.adapter;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.TokenBalanceRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenBalance;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataTokenBalanceRepository;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper.TokenBalancePersistenceMapper;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.Optional;

@Component
public class TokenBalanceRepositoryAdapter implements TokenBalanceRepositoryPort {
    private final SpringDataTokenBalanceRepository repository;
    private final TokenBalancePersistenceMapper mapper;

    public TokenBalanceRepositoryAdapter(SpringDataTokenBalanceRepository repository, TokenBalancePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public TokenBalance save(TokenBalance tokenBalance) {
        var entity = repository.save(mapper.toEntity(tokenBalance));

        return mapper.toDomain(entity);
    }

    @Override
    public Optional<TokenBalance> findByUserId(Long userId) {
        return repository.findByUserId(userId).map(mapper::toDomain);
    }
}
