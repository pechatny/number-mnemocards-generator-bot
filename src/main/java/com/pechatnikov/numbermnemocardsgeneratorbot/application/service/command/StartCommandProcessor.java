package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import org.springframework.stereotype.Component;

@Component
public class StartCommandProcessor implements CommandProcessor {
    private final SendMessageService sendMessageService;

    public StartCommandProcessor(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;
    }

    @Override
    public Command getCommand() {
        return Command.START;
    }

    @Override
    public void process(Long telegramId, Long chatId) {
        sendMessageService.sendMessage(chatId, "Добро пожаловать в мнемобот!");
    }
}
