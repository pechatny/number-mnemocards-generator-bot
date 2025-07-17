package com.pechatnikov.numbermnemocardsgeneratorbot.bot;

import com.pechatnikov.numbermnemocardsgeneratorbot.config.BotProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.service.ImageMerger;
import com.pechatnikov.numbermnemocardsgeneratorbot.service.NumberSplitter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
public class MnemocardsGeneratorBot extends TelegramLongPollingBot {

    private final Logger logger = LoggerFactory.getLogger(MnemocardsGeneratorBot.class);
    private final NumberSplitter splitter;
    private final ImageMerger imageMerger;
    private final BotProperties props;

    public MnemocardsGeneratorBot(
        NumberSplitter splitter,
        ImageMerger imageMerger,
        BotProperties props
    ) {
        this.splitter = splitter;
        this.imageMerger = imageMerger;
        this.props = props;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
//            Chat chat = update.getMessage().getChat();
//            String username = chat.getUserName();
//            String firstname = chat.getFirstName();
//            String lastname = chat.getLastName();

            String message = update.getMessage().getText();
            if (chatId == props.getChatId()) {
                List<String> numbers = splitter.split(message);
                File mergedPhoto = imageMerger.mergeImages(numbers);
                try {
                    execute(getSendPhotoCommand(update.getMessage().getChatId(), mergedPhoto));
                    deleteFile(mergedPhoto);

                } catch (TelegramApiException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private void deleteFile(File mergedPhoto) throws IOException {
        boolean deleted = Files.deleteIfExists(mergedPhoto.toPath());
        if(deleted){
            logger.info("File deleted: {}",  mergedPhoto.getName());
        } else{
            logger.error("Unable to delete file: {}", mergedPhoto.getName());
        }
    }

    private SendPhoto getSendPhotoCommand(Long chatId, File photo) {
        return new SendPhoto(
            String.valueOf(chatId),
            new InputFile(photo)
        );
    }

    @Override
    public String getBotUsername() {
        return props.getName();
    }

    @Override
    public String getBotToken() {
        return props.getToken();
    }
}
