package com.pechatnikov.numbermnemocardsgeneratorbot.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class AllNumbersSplitterTest {
    @ParameterizedTest
    @MethodSource("provideTestData")
    void split(String inputNumber, String[] expected) {
        var splitter = new NumberSplitter();
        List<String> resultList = splitter.split(inputNumber);

        assertArrayEquals(expected, resultList.toArray());
    }

    public static Stream<Arguments> provideTestData() {
        return Stream.of(
            Arguments.of("9992223344", new String[]{"999", "222", "33", "44"}),
            Arguments.of("99922233", new String[]{"999", "222", "33"}),
            Arguments.of("999222", new String[]{"999", "222"}),
            Arguments.of("99922233441", new String[]{"999", "222", "334", "41"}),
            Arguments.of("9992223", new String[]{"999", "22", "23"}),
            Arguments.of("99", new String[]{"99"}),
            Arguments.of("9988", new String[]{"99", "88"}),
            Arguments.of("999", new String[]{"999"})
        );
    }
}