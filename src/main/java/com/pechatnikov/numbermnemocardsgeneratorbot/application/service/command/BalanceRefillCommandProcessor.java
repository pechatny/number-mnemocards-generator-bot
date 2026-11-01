package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandProcessor;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.ShowPricesButtonUseCase;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BalanceRefillCommandProcessor implements CommandProcessor {
    private final ShowPricesButtonUseCase showPricesButtonUseCase;

    public BalanceRefillCommandProcessor(ShowPricesButtonUseCase showPricesButtonUseCase) {
        this.showPricesButtonUseCase = showPricesButtonUseCase;
    }

    @Override
    public Command getCommand() {
        return Command.BALANCE_REFILL;
    }

    @Override
    public void process(CommandContext context) {
        log.debug("Получена команда показать цена на пополнение баланса");
        showPricesButtonUseCase.showPricesButton(context.getPlatform(), context.getChatId());
    }

}
