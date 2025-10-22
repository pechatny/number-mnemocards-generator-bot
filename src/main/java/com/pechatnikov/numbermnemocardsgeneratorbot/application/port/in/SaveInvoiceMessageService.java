package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoiceMessage;

public interface SaveInvoiceMessageService {
    void saveInvoiceMessage(Long orderId, InvoiceMessage message);
}
