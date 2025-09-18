package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.MessageEntity;
import org.springframework.data.repository.CrudRepository;

public interface SpringDataMessageRepository extends CrudRepository<MessageEntity, Long> {
}
