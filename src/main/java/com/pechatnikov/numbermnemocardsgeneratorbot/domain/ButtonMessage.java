package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;

public final class ButtonMessage {
    private final Long chatId;
    private final String message;
    private final String buttonText;
    private final CallbackType callbackType;

    ButtonMessage(Long chatId, String message, String buttonText, CallbackType callbackType) {
        this.chatId = chatId;
        this.message = message;
        this.buttonText = buttonText;
        this.callbackType = callbackType;
    }

    public static ButtonMessageBuilder builder() {
        return new ButtonMessageBuilder();
    }

    public Long getChatId() {
        return this.chatId;
    }

    public String getMessage() {
        return this.message;
    }

    public String getButtonText() {
        return this.buttonText;
    }

    public CallbackType getCallbackType() {
        return this.callbackType;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ButtonMessage)) return false;
        final ButtonMessage other = (ButtonMessage) o;
        final Object this$chatId = this.getChatId();
        final Object other$chatId = other.getChatId();
        if (this$chatId == null ? other$chatId != null : !this$chatId.equals(other$chatId)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        final Object this$buttonText = this.getButtonText();
        final Object other$buttonText = other.getButtonText();
        if (this$buttonText == null ? other$buttonText != null : !this$buttonText.equals(other$buttonText))
            return false;
        final Object this$callbackType = this.getCallbackType();
        final Object other$callbackType = other.getCallbackType();
        if (this$callbackType == null ? other$callbackType != null : !this$callbackType.equals(other$callbackType))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $chatId = this.getChatId();
        result = result * PRIME + ($chatId == null ? 43 : $chatId.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        final Object $buttonText = this.getButtonText();
        result = result * PRIME + ($buttonText == null ? 43 : $buttonText.hashCode());
        final Object $callbackType = this.getCallbackType();
        result = result * PRIME + ($callbackType == null ? 43 : $callbackType.hashCode());
        return result;
    }

    public String toString() {
        return "ButtonMessage(chatId=" + this.getChatId() + ", message=" + this.getMessage() + ", buttonText=" + this.getButtonText() + ", callbackType=" + this.getCallbackType() + ")";
    }

    public static class ButtonMessageBuilder {
        private Long chatId;
        private String message;
        private String buttonText;
        private CallbackType callbackType;

        ButtonMessageBuilder() {
        }

        public ButtonMessageBuilder chatId(Long chatId) {
            this.chatId = chatId;
            return this;
        }

        public ButtonMessageBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ButtonMessageBuilder buttonText(String buttonText) {
            this.buttonText = buttonText;
            return this;
        }

        public ButtonMessageBuilder callbackType(CallbackType callbackType) {
            this.callbackType = callbackType;
            return this;
        }

        public ButtonMessage build() {
            return new ButtonMessage(this.chatId, this.message, this.buttonText, this.callbackType);
        }

        public String toString() {
            return "ButtonMessage.ButtonMessageBuilder(chatId=" + this.chatId + ", message=" + this.message + ", buttonText=" + this.buttonText + ", callbackType=" + this.callbackType + ")";
        }
    }
}
