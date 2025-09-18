package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.adapter;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.MessageRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataMessageRepository;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper.MessagePersistenceMapper;
import org.springframework.stereotype.Component;

@Component
public class MessageRepositoryAdapter implements MessageRepositoryPort {
    private final SpringDataMessageRepository springDataMessageRepository;
    private final MessagePersistenceMapper messagePersistenceMapper;

    public MessageRepositoryAdapter(SpringDataMessageRepository springDataMessageRepository, MessagePersistenceMapper messagePersistenceMapper) {
        this.springDataMessageRepository = springDataMessageRepository;
        this.messagePersistenceMapper = messagePersistenceMapper;
    }

    @Override
    public Message save(Message message) {
        var entity = springDataMessageRepository.save(
            messagePersistenceMapper.toEntity(message)
        );

        return messagePersistenceMapper.toDomain(entity);
    }
}
