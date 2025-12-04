package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoiceMessage;

public interface SendInvoiceService {
    InvoiceMessage send(Invoice invoice);
}
