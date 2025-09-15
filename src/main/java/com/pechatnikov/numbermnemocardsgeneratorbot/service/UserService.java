package com.pechatnikov.numbermnemocardsgeneratorbot.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.entity.User;
import com.pechatnikov.numbermnemocardsgeneratorbot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Instant;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getOrCreate(Update update) {
        Long chatId = update.getMessage().getChatId();
        User user = userRepository.findByChatId(chatId).orElseGet(() ->
                create(update)
        );

        return user;
    }

    private User create(Update update) {
        Chat chat = update.getMessage().getChat();
        return userRepository.save(User.builder()
                .chatId(chat.getId())
                .username(chat.getUserName())
                .firstname(chat.getFirstName())
                .lastname(chat.getLastName())
                .createdAt(Instant.now())
                .build()
        );

    }
}
