package com.pechatnikov.numbermnemocardsgeneratorbot.repository;

import com.pechatnikov.numbermnemocardsgeneratorbot.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByChatId(Long chatId);
}
