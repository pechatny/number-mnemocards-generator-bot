package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;

public interface InvoiceService {
    Invoice create(User user, String chatId, String title, String description, Money price);
}
