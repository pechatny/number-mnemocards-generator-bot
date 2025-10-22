package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

public final class InvoiceMessage {
    private final Long chatId;
    private final Integer messageId;

    InvoiceMessage(Long chatId, Integer messageId) {
        this.chatId = chatId;
        this.messageId = messageId;
    }

    public static InvoiceMessageBuilder builder() {
        return new InvoiceMessageBuilder();
    }

    public Long getChatId() {
        return this.chatId;
    }

    public Integer getMessageId() {
        return this.messageId;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof InvoiceMessage)) return false;
        final InvoiceMessage other = (InvoiceMessage) o;
        final Object this$chatId = this.getChatId();
        final Object other$chatId = other.getChatId();
        if (this$chatId == null ? other$chatId != null : !this$chatId.equals(other$chatId)) return false;
        final Object this$messageId = this.getMessageId();
        final Object other$messageId = other.getMessageId();
        if (this$messageId == null ? other$messageId != null : !this$messageId.equals(other$messageId)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $chatId = this.getChatId();
        result = result * PRIME + ($chatId == null ? 43 : $chatId.hashCode());
        final Object $messageId = this.getMessageId();
        result = result * PRIME + ($messageId == null ? 43 : $messageId.hashCode());
        return result;
    }

    public String toString() {
        return "InvoiceMessage(chatId=" + this.getChatId() + ", messageId=" + this.getMessageId() + ")";
    }

    public static class InvoiceMessageBuilder {
        private Long chatId;
        private Integer messageId;

        InvoiceMessageBuilder() {
        }

        public InvoiceMessageBuilder chatId(Long chatId) {
            this.chatId = chatId;
            return this;
        }

        public InvoiceMessageBuilder messageId(Integer messageId) {
            this.messageId = messageId;
            return this;
        }

        public InvoiceMessage build() {
            return new InvoiceMessage(this.chatId, this.messageId);
        }

        public String toString() {
            return "InvoiceMessage.InvoiceMessageBuilder(chatId=" + this.chatId + ", messageId=" + this.messageId + ")";
        }
    }
}
