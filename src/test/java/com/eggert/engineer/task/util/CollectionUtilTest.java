package com.eggert.engineer.task.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionUtilTest {

    private static Stream<Arguments> batchesSource() {
        return Stream.of(
                Arguments.of(List.of(1, 2, 3), 1, List.of(List.of(1), List.of(2), List.of(3))),
                Arguments.of(List.of(1, 2, 3), 2, List.of(List.of(1, 2), List.of(3))),
                Arguments.of(List.of(1, 2, 3, 4), 2, List.of(List.of(1, 2), List.of(3, 4)))
        );
    }

    @ParameterizedTest
    @MethodSource("batchesSource")
    void batches(List<Integer> inputList, int batchSize, List<List<Integer>> batchedList) {
        assertThat(CollectionUtil.batches(inputList, batchSize)).isEqualTo(batchedList);
    }
}