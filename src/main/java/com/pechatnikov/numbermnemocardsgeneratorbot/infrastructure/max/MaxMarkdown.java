package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import java.util.regex.Pattern;

/**
 * Тексты бота написаны в Telegram Markdown, где {@code *текст*} — жирный.
 * В Max Markdown (CommonMark) {@code *текст*} — курсив, а жирный — {@code **текст**}.
 */
final class MaxMarkdown {
    // Одиночная звёздочка, не являющаяся частью "**", вокруг текста без звёздочек и переносов строк
    private static final Pattern TELEGRAM_BOLD = Pattern.compile("(?<!\\*)\\*(?!\\*)([^*\\n]+?)(?<!\\*)\\*(?!\\*)");

    private MaxMarkdown() {
    }

    static String fromTelegramMarkdown(String text) {
        if (text == null) {
            return null;
        }
        return TELEGRAM_BOLD.matcher(text).replaceAll("**$1**");
    }
}
