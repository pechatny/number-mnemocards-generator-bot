package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

public final class ButtonData {
    private final String buttonText;
    private final String buttonCallbackData;

    ButtonData(String buttonText, String buttonCallbackData) {
        this.buttonText = buttonText;
        this.buttonCallbackData = buttonCallbackData;
    }

    public static ButtonDataBuilder builder() {
        return new ButtonDataBuilder();
    }

    public String getButtonText() {
        return this.buttonText;
    }

    public String getButtonCallbackData() {
        return this.buttonCallbackData;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ButtonData)) return false;
        final ButtonData other = (ButtonData) o;
        final Object this$buttonText = this.getButtonText();
        final Object other$buttonText = other.getButtonText();
        if (this$buttonText == null ? other$buttonText != null : !this$buttonText.equals(other$buttonText))
            return false;
        final Object this$buttonCallbackData = this.getButtonCallbackData();
        final Object other$buttonCallbackData = other.getButtonCallbackData();
        if (this$buttonCallbackData == null ? other$buttonCallbackData != null : !this$buttonCallbackData.equals(other$buttonCallbackData))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $buttonText = this.getButtonText();
        result = result * PRIME + ($buttonText == null ? 43 : $buttonText.hashCode());
        final Object $buttonCallbackData = this.getButtonCallbackData();
        result = result * PRIME + ($buttonCallbackData == null ? 43 : $buttonCallbackData.hashCode());
        return result;
    }

    public String toString() {
        return "ButtonData(buttonText=" + this.getButtonText() + ", buttonCallbackData=" + this.getButtonCallbackData() + ")";
    }

    public static class ButtonDataBuilder {
        private String buttonText;
        private String buttonCallbackData;

        ButtonDataBuilder() {
        }

        public ButtonDataBuilder buttonText(String buttonText) {
            this.buttonText = buttonText;
            return this;
        }

        public ButtonDataBuilder buttonCallbackData(String buttonCallbackData) {
            this.buttonCallbackData = buttonCallbackData;
            return this;
        }

        public ButtonData build() {
            return new ButtonData(this.buttonText, this.buttonCallbackData);
        }

        public String toString() {
            return "ButtonData.ButtonDataBuilder(buttonText=" + this.buttonText + ", buttonCallbackData=" + this.buttonCallbackData + ")";
        }
    }
}
