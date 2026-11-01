package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendCallbackAnswerService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Нажатие кнопки-команды выполняет ту же команду, что и ввод её текстом.
 */
@Slf4j
@Component
public class RunCommandCallbackProcessor implements CallbackProcessor {
    private final CommandHandler commandHandler;
    private final SendCallbackAnswerService sendCallbackAnswerService;

    public RunCommandCallbackProcessor(CommandHandler commandHandler, SendCallbackAnswerService sendCallbackAnswerService) {
        this.commandHandler = commandHandler;
        this.sendCallbackAnswerService = sendCallbackAnswerService;
    }

    @Override
    public CallbackType getCallBacktype() {
        return CallbackType.RUN_COMMAND;
    }

    @Override
    public void process(Callback callback) {
        Command command = Command.valueOf(callback.getValue());
        log.debug("Нажата кнопка команды {}", command);

        // Max не принимает ответ без текста (400 proto.payload), поэтому коротко показываем, что выполняется
        sendCallbackAnswerService.sendCallbackAnswer(callback.getPlatform(), callback.getCallbackQueryId(), command.getDescription());

        commandHandler.handle(command, CommandContext.builder()
            .platform(callback.getPlatform())
            .externalUserId(callback.getExternalUserId())
            .chatId(callback.getChatId())
            .build());
    }
}
