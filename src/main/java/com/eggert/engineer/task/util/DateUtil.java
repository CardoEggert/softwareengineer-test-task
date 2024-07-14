package com.eggert.engineer.task.util;

import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class DateUtil {

    private DateUtil(){}

    /**
     * Helper method to split a date range into separate ranges
     * @param start - start of the range
     * @param end - end of the range
     * @param stepInDays - should the step be made in a day or a week
     * @return a list of ranges in days/week, depending on the stepInDays param
     */
    public static List<Pair<LocalDate, LocalDate>> splitRange(LocalDate start, LocalDate end, boolean stepInDays) {
        final List<Pair<LocalDate, LocalDate>> ranges = new ArrayList<>();
        LocalDate currentStart = start;
        while (currentStart.isBefore(end)) {
            LocalDate currentEnd = stepInDays ? currentStart.plusDays(1) : currentStart.plusDays(6);
            if (currentEnd.isAfter(end)) {
                currentEnd = end;
            }
            ranges.add(Pair.of(currentStart, currentEnd));
            currentStart = stepInDays ? currentEnd : currentEnd.plusDays(1);
        }
        return ranges;
    }
}
