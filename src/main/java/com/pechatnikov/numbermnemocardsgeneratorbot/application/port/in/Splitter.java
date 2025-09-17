package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import java.util.List;

public interface Splitter {
    List<String> split(String number);
}
