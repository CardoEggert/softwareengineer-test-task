package com.eggert.engineer.task.unit.util;

import com.eggert.engineer.task.util.CollectionUtil;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CollectionUtilTest {

  private static Stream<Arguments> batchesSource() {
    return Stream.of(
        Arguments.of(List.of(1, 2, 3), 1, List.of(List.of(1), List.of(2), List.of(3))),
        Arguments.of(List.of(1, 2, 3), 2, List.of(List.of(1, 2), List.of(3))),
        Arguments.of(List.of(1, 2, 3, 4), 2, List.of(List.of(1, 2), List.of(3, 4))));
  }

  @ParameterizedTest
  @MethodSource("batchesSource")
  void batches(List<Integer> inputList, int batchSize, List<List<Integer>> batchedList) {
    Assertions.assertThat(CollectionUtil.batches(inputList, batchSize)).isEqualTo(batchedList);
  }
}
