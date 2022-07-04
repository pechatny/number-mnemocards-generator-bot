package com.pechatnikov.numbermnemocardsgeneratorbot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NumberSplitter implements Splitter {

    @Override
    public List<String> split(String number) {
        List<String> groups = groupNumber(number);
        List<String> accumulator = new ArrayList<>();
        for (String group : groups) {
            accumulator.addAll(splitNumber(group));
        }

        return accumulator;
    }

    private List<String> groupNumber(String number) {
        String pattern = "(\\d+)";
        Pattern regexp = Pattern.compile(pattern);
        Matcher matcher = regexp.matcher(number);
        List<String> groups = new ArrayList<>();

        while (matcher.find()) {
            groups.add(matcher.group(1));
        }

        return groups;
    }

    private List<String> splitNumber(String number) {
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
