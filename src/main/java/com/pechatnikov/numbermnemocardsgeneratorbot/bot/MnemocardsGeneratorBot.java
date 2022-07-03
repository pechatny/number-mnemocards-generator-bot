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
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Component
public class MnemocardsGeneratorBot extends TelegramLongPollingBot {

    private final Logger logger = LoggerFactory.getLogger(MnemocardsGeneratorBot.class);
    private final NumberSplitter splitter;
    private final ImageMerger imageMerger;
    private final BotProperties props;
    private static final int CHAT_ID = 78477044;

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
            String message = update.getMessage().getText();
            if (chatId == CHAT_ID && isNumeric(message)) {
                List<String> numbers = splitter.split(message);
                File mergedPhoto = imageMerger.mergeImages(numbers);
                try {
                    execute(getSendPhotoCommand(update.getMessage().getChatId(), mergedPhoto));
                } catch (TelegramApiException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }



    private SendPhoto getSendPhotoCommand(Long chatId, File photo) {
        return new SendPhoto(
            String.valueOf(chatId),
            new InputFile(photo)
        );
    }

    private SendPhoto getSendPhotoCommand(Long chatId, String number) {
        return new SendPhoto(
            String.valueOf(chatId),
            new InputFile(
                new File(Objects.requireNonNull(
                    getClass().getResource(getImageFilename(number))).getFile()
                )
            )
        );
    }

    private String getImageFilename(String name) {
        return "/numbercards/" + name + ".jpg";
    }

    @Override
    public String getBotUsername() {
        return props.getName();
    }

    @Override
    public String getBotToken() {
        return props.getToken();
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
