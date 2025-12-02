package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandProcessor;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class StartCommandProcessor implements CommandProcessor {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private final SendMessageService sendMessageService;
    private final static String START_MESSAGE = "*Добро пожаловать в бот для генерации образных кодов!*\n" +
        "\n" +
        "Этот бот помогает запоминать любые числа с помощью мнемотехники (системы «Джордано»). \n" +
        "\n" +
        "*Как это работает:*  \n" +
        "1. Вы отправляете боту любое число, например номер телефона.  \n" +
        "2. Бот преобразует цифры в слова, имеющие яркие зрительные образы.  \n" +
        "3. Вы получаете картинку, в которой зашифрован номер — её легко запомнить.\n";

    private final static String START_MESSAGE_2 = "*Основа метода: буквенно‑цифровой код*  \n" +
        "Каждой цифре от 0 до 9 соответствуют две согласные буквы русского алфавита:\n" +
        "```\n" +
        "0 - НМ\n" +
        "1 - ГЖ\n" +
        "2 - ДТ\n" +
        "3 - КХ\n" +
        "4 - ЧЩ\n" +
        "5 - ПБ\n" +
        "6 - ШЛ\n" +
        "7 - СЗ\n" +
        "8 - ВФ\n" +
        "9 - РЦ" +
        "```\n" +
        "*Примеры кодирования:*  \n" +
        "- Число **25**: 2→Д,Т; 5→П,Б. Слово: «ТоПор» (Т=2, П=5).  \n" +
        "- Число **390**: 3→К,Х; 9→Р,Ц; 0→Н,М. Слово: «КоРоНа».\n" +
        "\n";

    private final static String START_MESSAGE_3 =
        "*Как отправлять номер телефона:*  \n" +
            "- Лучше **не указывать** начальную цифру *+7* или *8* — она всегда одинаковая.  \n" +
            "- Можно отправлять номер в любом формате: бот автоматически удалит лишние символы.  \n" +
            "  Например: `(968) 666 33‑44` или `9686663344`.  \n" +
            "- Бот разобьёт число на группы по 2–3 цифры и создаст карточки для запоминания.\n" +
            "\n" +
            "*Пример разбиения:*  \n" +
            "Номер *9686663344* будет разбит на группы:  \n" +
            "`968`, `666`, `33`, `44` — получится *4 карточки*.\n" +
            "\n" +
            "*Что вы получите:*  \n" +
            "- Карточки с образными кодами для каждой группы цифр.  \n" +
            "- Единую картинку, объединяющую все карточки.  \n" +
            "- Возможность открыть картинку для просмотра или сохранить её на телефон для повторения.\n" +
            "\n" +
            "Узнайте больше о системе запоминания: \n" +
            "https://mnemonikon.ru";

    public StartCommandProcessor(SendMessageService sendMessageService) {
        this.sendMessageService = sendMessageService;
    }

    @Override
    public Command getCommand() {
        return Command.START;
    }

    @Override
    public void process(Long telegramId, Long chatId) {
        sendMessageService.sendMessage(chatId, START_MESSAGE);

        scheduler.schedule(() ->
                sendMessageService.sendMessage(chatId, START_MESSAGE_2),
            15,
            TimeUnit.SECONDS
        );

        scheduler.schedule(
            () -> sendMessageService.sendMessage(chatId, START_MESSAGE_3),
            35,
            TimeUnit.SECONDS
        );
    }
}
