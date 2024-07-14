package com.eggert.engineer.task.unit.util;

import com.eggert.engineer.task.util.DateUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilTest {

    // TODO: More test cases (cases like leap year and etc.)
    private static Stream<Arguments> previousPeriodSource() {
        return Stream.of(
                Arguments.of(
                        LocalDate.of(2020, 2, 15),
                        LocalDate.of(2020, 2, 21),
                        LocalDate.of(2020, 2, 8),
                        LocalDate.of(2020, 2, 14)),
                Arguments.of(
                        LocalDate.of(2019, 1, 1),
                        LocalDate.of(2019, 1,2),
                        LocalDate.of(2018, 12, 30),
                        LocalDate.of(2018, 12,31))
        );
    }

    @ParameterizedTest
    @MethodSource("previousPeriodSource")
    void previousPeriod(LocalDate start, LocalDate end, LocalDate expectedPreviousDateStart, LocalDate expectedPreviousDateEnd) {
        Assertions.assertThat(DateUtil.previousPeriod(start, end)).isEqualTo(Pair.of(expectedPreviousDateStart, expectedPreviousDateEnd));
    }

    // TODO: More test cases
    private static Stream<Arguments> splitRangeSource() {
        return Stream.of(
                Arguments.of(
                        LocalDate.of(2019, 1, 1),
                        LocalDate.of(2019, 1,1),
                        true,
                        List.of()),
                Arguments.of(
                        LocalDate.of(2019, 1, 1),
                        LocalDate.of(2019, 1,2),
                        true,
                        List.of(Pair.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 2)))),
                Arguments.of(
                        LocalDate.of(2019, 1, 1),
                        LocalDate.of(2019, 1,3),
                        true,
                        List.of(
                                Pair.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 2)),
                                Pair.of(LocalDate.of(2019, 1, 2), LocalDate.of(2019, 1, 3))
                        )),
                Arguments.of(
                        LocalDate.of(2019, 1, 1),
                        LocalDate.of(2019, 1,31),
                        false,
                        List.of(
                                Pair.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 7)),
                                Pair.of(LocalDate.of(2019, 1, 8), LocalDate.of(2019, 1, 14)),
                                Pair.of(LocalDate.of(2019, 1, 15), LocalDate.of(2019, 1, 21)),
                                Pair.of(LocalDate.of(2019, 1, 22), LocalDate.of(2019, 1, 28)),
                                Pair.of(LocalDate.of(2019, 1, 29), LocalDate.of(2019, 1, 31))
                        )),
                Arguments.of(
                        LocalDate.of(2019, 2, 1),
                        LocalDate.of(2019, 2,28),
                        false,
                        List.of(
                                Pair.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 7)),
                                Pair.of(LocalDate.of(2019, 2, 8), LocalDate.of(2019, 2, 14)),
                                Pair.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 21)),
                                Pair.of(LocalDate.of(2019, 2, 22), LocalDate.of(2019, 2, 28))
                        ))
        );
    }

    @ParameterizedTest
    @MethodSource("splitRangeSource")
    void splitRange(LocalDate start, LocalDate end, boolean stepInDays, List<Pair<LocalDate, LocalDate>> ranges) {
        assertThat(DateUtil.splitRange(start, end, stepInDays)).isEqualTo(ranges);
    }
}
