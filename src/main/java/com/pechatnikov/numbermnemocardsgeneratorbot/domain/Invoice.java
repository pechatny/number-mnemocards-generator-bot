package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

public class Invoice {
    private final String chatId;
    private final String title;
    private final String description;
    private final Payload payload;
    private final Money price;
    private final ProviderData providerData;

    Invoice(String chatId, String title, String description, Payload payload, Money price, ProviderData providerData) {
        this.chatId = chatId;
        this.title = title;
        this.description = description;
        this.payload = payload;
        this.price = price;
        this.providerData = providerData;
    }

    public static InvoiceBuilder builder() {
        return new InvoiceBuilder();
    }

    public String getChatId() {
        return this.chatId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public Payload getPayload() {
        return this.payload;
    }

    public Money getPrice() {
        return this.price;
    }

    public ProviderData getProviderData() {
        return providerData;
    }

    public InvoiceBuilder toBuilder() {
        return new InvoiceBuilder()
            .chatId(this.chatId)
            .title(this.title)
            .description(this.description)
            .payload(this.payload)
            .price(this.price)
            .providerData(this.providerData);
    }

    public static class InvoiceBuilder {
        private String chatId;
        private String title;
        private String description;
        private Payload payload;
        private Money price;
        private ProviderData providerData;

        InvoiceBuilder() {
        }

        public InvoiceBuilder chatId(String chatId) {
            this.chatId = chatId;
            return this;
        }

        public InvoiceBuilder title(String title) {
            this.title = title;
            return this;
        }

        public InvoiceBuilder description(String description) {
            this.description = description;
            return this;
        }

        public InvoiceBuilder payload(Payload payload) {
            this.payload = payload;
            return this;
        }

        public InvoiceBuilder price(Money price) {
            this.price = price;
            return this;
        }

        public InvoiceBuilder providerData(ProviderData providerData) {
            this.providerData = providerData;
            return this;
        }

        public Invoice build() {
            return new Invoice(this.chatId, this.title, this.description, this.payload, this.price, this.providerData);
        }

        public String toString() {
            return "Invoice.InvoiceBuilder(chatId=" + this.chatId + ", title=" + this.title + ", description=" + this.description + ", payload=" + this.payload + ", price=" + this.price + ")";
        }
    }

    public static class Payload {
        private final Long orderId;

        public Payload(Long orderId) {
            this.orderId = orderId;
        }

        public Long getOrderId() {
            return orderId;
        }
    }
}
