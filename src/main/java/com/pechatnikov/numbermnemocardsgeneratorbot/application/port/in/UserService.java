package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;

public interface UserService {
    User getOrCreateUser(GetOrCreateUserCommand getOrCreateUserCommand);
}
