package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandProcessor;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenBalanceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenBalance;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class BalanceCommandProcessor implements CommandProcessor {
    private final SendMessageService sendMessageService;
    private final TokenBalanceService tokenBalanceService;
    private final UserService userService;

    public BalanceCommandProcessor(SendMessageService sendMessageService, TokenBalanceService tokenBalanceService, UserService userService) {
        this.sendMessageService = sendMessageService;
        this.tokenBalanceService = tokenBalanceService;
        this.userService = userService;
    }

    @Override
    public Command getCommand() {
        return Command.BALANCE;
    }

    @Override
    public void process(Long telegramId, Long chatId) {
        User user = userService.findByTelegramId(telegramId).get();
        TokenBalance balanceValue = tokenBalanceService.findOrCreateTokenBalanceForUser(user);
        String message = String.format("Ваш баланс цифр %d шт.", balanceValue.getBalance());

        sendMessageService.sendMessage(chatId, message);
    }

}
