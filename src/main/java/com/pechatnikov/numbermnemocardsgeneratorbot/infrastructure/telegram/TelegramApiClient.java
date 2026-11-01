package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnTelegramBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.TelegramBotProperties;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@ConditionalOnTelegramBot
@Service
public class TelegramApiClient extends DefaultAbsSender {

    private final TelegramBotProperties props;

    protected TelegramApiClient(TelegramBotProperties props) {
        super(new DefaultBotOptions());
        this.props = props;
    }

    @Override
    public String getBotToken() {
        return props.getToken();
    }
}
