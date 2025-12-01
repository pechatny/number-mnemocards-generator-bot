package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import java.util.List;

public final class ButtonsMessage {
    private final String chatId;
    private final String message;
    private final List<ButtonData> buttons;

    ButtonsMessage(String chatId, String message, List<ButtonData> buttons) {
        this.chatId = chatId;
        this.message = message;
        this.buttons = buttons;
    }

    public static ButtonsMessageBuilder builder() {
        return new ButtonsMessageBuilder();
    }

    public String getChatId() {
        return this.chatId;
    }

    public String getMessage() {
        return this.message;
    }

    public List<ButtonData> getButtons() {
        return this.buttons;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ButtonsMessage)) return false;
        final ButtonsMessage other = (ButtonsMessage) o;
        final Object this$chatId = this.getChatId();
        final Object other$chatId = other.getChatId();
        if (this$chatId == null ? other$chatId != null : !this$chatId.equals(other$chatId)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        final Object this$buttons = this.getButtons();
        final Object other$buttons = other.getButtons();
        if (this$buttons == null ? other$buttons != null : !this$buttons.equals(other$buttons)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $chatId = this.getChatId();
        result = result * PRIME + ($chatId == null ? 43 : $chatId.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        final Object $buttons = this.getButtons();
        result = result * PRIME + ($buttons == null ? 43 : $buttons.hashCode());
        return result;
    }

    public String toString() {
        return "ButtonsMessage(chatId=" + this.getChatId() + ", message=" + this.getMessage() + ", buttons=" + this.getButtons() + ")";
    }

    public static class ButtonsMessageBuilder {
        private String chatId;
        private String message;
        private List<ButtonData> buttons;

        ButtonsMessageBuilder() {
        }

        public ButtonsMessageBuilder chatId(String chatId) {
            this.chatId = chatId;
            return this;
        }

        public ButtonsMessageBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ButtonsMessageBuilder buttons(List<ButtonData> buttons) {
            this.buttons = buttons;
            return this;
        }

        public ButtonsMessage build() {
            return new ButtonsMessage(this.chatId, this.message, this.buttons);
        }

        public String toString() {
            return "ButtonsMessage.ButtonsMessageBuilder(chatId=" + this.chatId + ", message=" + this.message + ", buttons=" + this.buttons + ")";
        }
    }
}
