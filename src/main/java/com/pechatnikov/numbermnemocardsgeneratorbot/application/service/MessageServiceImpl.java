package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.MessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.MessageRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepositoryPort messageRepositoryPort;

    public MessageServiceImpl(MessageRepositoryPort messageRepositoryPort) {
        this.messageRepositoryPort = messageRepositoryPort;
    }

    @Override
    public void save(Message message) {
        messageRepositoryPort.save(message);

    }
}
