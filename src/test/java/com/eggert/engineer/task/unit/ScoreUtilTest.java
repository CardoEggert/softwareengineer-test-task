package com.eggert.engineer.task.unit;

import com.eggert.engineer.task.util.ScoreUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ScoreUtilTest {

    private static Stream<Arguments> scoreSource() {
        return Stream.of(
                Arguments.of(valueOf(-1), valueOf(5), ZERO),
                Arguments.of(valueOf(1), valueOf(-1), ZERO),
                Arguments.of(ZERO, ZERO, ZERO),
                Arguments.of(ZERO, ONE, ZERO),
                Arguments.of(ONE, ZERO, ZERO),
                Arguments.of(ONE, valueOf(1), valueOf(20)),
                Arguments.of(ONE, valueOf(2), valueOf(40)),
                Arguments.of(ONE, valueOf(3), valueOf(60)),
                Arguments.of(ONE, valueOf(4), valueOf(80)),
                Arguments.of(ONE, valueOf(5), valueOf(100)),
                Arguments.of(valueOf(0.7), valueOf(1), valueOf(14)),
                Arguments.of(valueOf(0.7), valueOf(2), valueOf(28)),
                Arguments.of(valueOf(0.7), valueOf(3), valueOf(42)),
                Arguments.of(valueOf(0.7), valueOf(4), valueOf(56)),
                Arguments.of(valueOf(0.7), valueOf(5), valueOf(70)),
                Arguments.of(valueOf(1.2), valueOf(1), valueOf(24)),
                Arguments.of(valueOf(1.2), valueOf(2), valueOf(48)),
                Arguments.of(valueOf(1.2), valueOf(3), valueOf(72)),
                Arguments.of(valueOf(1.2), valueOf(4), valueOf(96)),
                Arguments.of(valueOf(1.2), valueOf(5), valueOf(100))
        );
    }
    // TODO: Add more complicated scenarios that are borderline regarding percentages
    // TODO: Add scenarios where the percentage result would not be an integer

    @ParameterizedTest
    @MethodSource(value = "scoreSource")
    void calculateScoreTests(BigDecimal categoryWeight, BigDecimal rating, BigDecimal expectedScore) {
        assertThat(ScoreUtil.calculateScore(categoryWeight, rating)).isEqualByComparingTo(expectedScore);
    }

    private static Stream<Arguments> averagePercentageSource() {
        return Stream.of(
                Arguments.of(List.of(), ZERO),
                Arguments.of(List.of(valueOf(10)), valueOf(10)),
                Arguments.of(List.of(valueOf(10), valueOf(20)), valueOf(15)),
                Arguments.of(List.of(valueOf(10), valueOf(20), valueOf(30)), valueOf(20))
        );
    }
    // TODO: More test cases to cover
    @ParameterizedTest
    @MethodSource(value = "averagePercentageSource")
    void percentageTests(List<BigDecimal> percentages, BigDecimal expectedPercentage) {
        assertThat(ScoreUtil.averagePercentage(percentages)).isEqualByComparingTo(expectedPercentage);
    }


}
