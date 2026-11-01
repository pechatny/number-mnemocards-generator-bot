package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxCallback;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxMessageBody;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxRecipient;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdate;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUser;

public final class MaxTestData {
    public static final long USER_ID = 7L;
    public static final long CHAT_ID = 42L;

    private MaxTestData() {
    }

    public static MaxUser user() {
        return MaxUser.builder().userId(USER_ID).firstName("Иван").lastName(null).username(null).isBot(false).build();
    }

    public static MaxMessage message(String text, String mid) {
        return MaxMessage.builder()
            .sender(user())
            .recipient(MaxRecipient.builder().chatId(CHAT_ID).chatType("dialog").build())
            .body(MaxMessageBody.builder().mid(mid).text(text).build())
            .build();
    }

    public static MaxUpdate messageCreated(String text) {
        return MaxUpdate.builder().updateType(MaxUpdate.MESSAGE_CREATED).message(message(text, "mid.1")).build();
    }

    public static MaxUpdate callback(String payload) {
        return MaxUpdate.builder()
            .updateType(MaxUpdate.MESSAGE_CALLBACK)
            .callback(MaxCallback.builder().callbackId("cb-1").payload(payload).user(user()).build())
            .message(message(null, "mid.2"))
            .build();
    }

    public static MaxUpdate botStarted() {
        return MaxUpdate.builder().updateType(MaxUpdate.BOT_STARTED).chatId(CHAT_ID).user(user()).build();
    }
}
