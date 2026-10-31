package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaxMarkdownTest {

    @Test
    void shouldConvertTelegramBoldToMaxBold() {
        assertThat(MaxMarkdown.fromTelegramMarkdown("*Как это работает:*  \n"))
            .isEqualTo("**Как это работает:**  \n");
    }

    @Test
    void shouldConvertSeveralBoldFragmentsInOneLine() {
        assertThat(MaxMarkdown.fromTelegramMarkdown("начальную цифру *+7* или *8* — она всегда одинаковая"))
            .isEqualTo("начальную цифру **+7** или **8** — она всегда одинаковая");
    }

    @Test
    void shouldKeepExistingDoubleAsterisks() {
        assertThat(MaxMarkdown.fromTelegramMarkdown("- Число **25**: 2→Д,Т; лучше **не указывать**"))
            .isEqualTo("- Число **25**: 2→Д,Т; лучше **не указывать**");
    }

    @Test
    void shouldKeepCodeAndPlainText() {
        String text = "```\n0 - НМ\n1 - ГЖ```\n`968`, `666` — Ваш баланс цифр 15 шт.";

        assertThat(MaxMarkdown.fromTelegramMarkdown(text)).isEqualTo(text);
    }

    @Test
    void shouldNotJoinAsterisksAcrossLines() {
        assertThat(MaxMarkdown.fromTelegramMarkdown("a * b\nc * d")).isEqualTo("a * b\nc * d");
    }

    @Test
    void shouldHandleNull() {
        assertThat(MaxMarkdown.fromTelegramMarkdown(null)).isNull();
    }
}
