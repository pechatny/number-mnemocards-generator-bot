package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

public final class PreCheckout {
    private final String id;
    private final Long orderId;
    private final Money paymentAmount;

    PreCheckout(String id, Long orderId, Money paymentAmount) {
        this.id = id;
        this.orderId = orderId;
        this.paymentAmount = paymentAmount;
    }

    public static PreCheckoutBuilder builder() {
        return new PreCheckoutBuilder();
    }

    public String getId() {
        return this.id;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public Money getPaymentAmount() {
        return this.paymentAmount;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PreCheckout)) return false;
        final PreCheckout other = (PreCheckout) o;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$orderId = this.getOrderId();
        final Object other$orderId = other.getOrderId();
        if (this$orderId == null ? other$orderId != null : !this$orderId.equals(other$orderId)) return false;
        final Object this$paymentAmount = this.getPaymentAmount();
        final Object other$paymentAmount = other.getPaymentAmount();
        if (this$paymentAmount == null ? other$paymentAmount != null : !this$paymentAmount.equals(other$paymentAmount))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $orderId = this.getOrderId();
        result = result * PRIME + ($orderId == null ? 43 : $orderId.hashCode());
        final Object $paymentAmount = this.getPaymentAmount();
        result = result * PRIME + ($paymentAmount == null ? 43 : $paymentAmount.hashCode());
        return result;
    }

    public String toString() {
        return "PreCheckout(id=" + this.getId() + ", orderId=" + this.getOrderId() + ", paymentAmount=" + this.getPaymentAmount() + ")";
    }

    public static class PreCheckoutBuilder {
        private String id;
        private Long orderId;
        private Money paymentAmount;

        PreCheckoutBuilder() {
        }

        public PreCheckoutBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PreCheckoutBuilder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public PreCheckoutBuilder paymentAmount(Money paymentAmount) {
            this.paymentAmount = paymentAmount;
            return this;
        }

        public PreCheckout build() {
            return new PreCheckout(this.id, this.orderId, this.paymentAmount);
        }

        public String toString() {
            return "PreCheckout.PreCheckoutBuilder(id=" + this.id + ", orderId=" + this.orderId + ", paymentAmount=" + this.paymentAmount + ")";
        }
    }
}
