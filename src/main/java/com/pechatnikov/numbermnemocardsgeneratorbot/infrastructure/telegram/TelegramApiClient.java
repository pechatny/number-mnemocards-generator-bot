package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.BotProperties;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Service
public class TelegramApiClient extends DefaultAbsSender {

    private final BotProperties props;

    protected TelegramApiClient(BotProperties props) {
        super(new DefaultBotOptions());
        this.props = props;
    }

    @Override
    public String getBotToken() {
        return props.getToken();
    }
}
