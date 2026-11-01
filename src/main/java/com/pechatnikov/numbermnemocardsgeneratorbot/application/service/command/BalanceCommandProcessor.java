package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandProcessor;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenBalanceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.TokenBalanceServiceImpl;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
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
    public void process(CommandContext context) {
        long balance = userService.findByPlatformAndExternalId(context.getPlatform(), context.getExternalUserId())
            .map(user -> tokenBalanceService.findOrCreateTokenBalanceForUser(user).getBalance())
            // Пользователь создаётся при первом числе, до этого у него стартовый баланс
            .orElse(TokenBalanceServiceImpl.NEW_USER_INIT_TOKENS);
        String message = String.format("Ваш баланс цифр %d шт.", balance);

        sendMessageService.sendMessage(context.getPlatform(), context.getChatId(), message);
    }

}
