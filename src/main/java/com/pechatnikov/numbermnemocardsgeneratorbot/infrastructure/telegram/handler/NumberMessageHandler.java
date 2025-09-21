package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.handler;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.BotProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.ImageMerger;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.NumberSplitter;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.SendPhotoAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
//@Component
public class NumberMessageHandler {
    private final NumberSplitter splitter;
    private final ImageMerger imageMerger;
    private final SendPhotoAdapter sendPhotoAdapter;

    public NumberMessageHandler(NumberSplitter splitter, ImageMerger imageMerger, SendPhotoAdapter sendPhotoAdapter) {
        this.splitter = splitter;
        this.imageMerger = imageMerger;
        this.sendPhotoAdapter = sendPhotoAdapter;
    }

    public SendPhoto handleMessage(Update update) throws IOException {
        String message = update.getMessage().getText();
        List<String> numbers = splitter.split(message);
        File mergedPhoto = imageMerger.mergeImages(numbers);

        sendPhotoAdapter.sendPhoto(update.getMessage().getChatId(), mergedPhoto);

        return getSendPhotoCommand(update.getMessage().getChatId(), mergedPhoto);
    }

    private SendPhoto getSendPhotoCommand(Long chatId, File photo) {
        return new SendPhoto(
            String.valueOf(chatId),
            new InputFile(photo)
        );
    }
}
