package com.pechatnikov.numbermnemocardsgeneratorbot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NumberSplitter implements Splitter {

    public List<String> split(String number) {
        List<String> numberAccumulator = new ArrayList<>();
        if (number.length() < 2) {
            return numberAccumulator;
        }

        if (number.length() % 2 == 0 && number.length() % 3 == 0) {
            while (number.length() > 0) {
                number = extractNumberGroup(number, 3, numberAccumulator);
            }
        } else {
            while (number.length() > 4 || number.length() == 3) {
                number = extractNumberGroup(number, 3, numberAccumulator);
            }

            while (number.length() > 0) {
                number = extractNumberGroup(number, 2, numberAccumulator);
            }
        }

        return numberAccumulator;
    }

    private String extractNumberGroup(String number, int groupSize, List<String> accumulator) {
        var extractedNumber = number.substring(0, groupSize);
        accumulator.add(extractedNumber);
        return number.substring(groupSize);
    }
}
