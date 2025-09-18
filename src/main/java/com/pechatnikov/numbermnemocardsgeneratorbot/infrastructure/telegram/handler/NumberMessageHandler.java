package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.handler;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.BotProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.ImageMerger;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.NumberSplitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class NumberMessageHandler {
    private final BotProperties props;
    private final NumberSplitter splitter;
    private final ImageMerger imageMerger;

    public NumberMessageHandler(BotProperties props, NumberSplitter splitter, ImageMerger imageMerger) {
        this.props = props;
        this.splitter = splitter;
        this.imageMerger = imageMerger;
    }

    public SendPhoto handleMessage(Update update) throws IOException {
//            User user = userService.getOrCreate(update);
        Long chatId = update.getMessage().getChatId();
//            Chat chat = update.getMessage().getChat();
//            String username = chat.getUserName();
//            String firstname = chat.getFirstName();
//            String lastname = chat.getLastName();

        String message = update.getMessage().getText();
        if (chatId == props.getChatId()) {
            List<String> numbers = splitter.split(message);
            File mergedPhoto = imageMerger.mergeImages(numbers);

            return getSendPhotoCommand(update.getMessage().getChatId(), mergedPhoto);
        }

        return null;
    }

    private SendPhoto getSendPhotoCommand(Long chatId, File photo) {
        return new SendPhoto(
            String.valueOf(chatId),
            new InputFile(photo)
        );
    }
}
