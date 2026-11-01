package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
public interface ShowPricesButtonUseCase {
    void showPricesButton(BotPlatform platform, Long chatId);
}
