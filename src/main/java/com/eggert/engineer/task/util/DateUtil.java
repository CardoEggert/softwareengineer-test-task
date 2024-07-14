package com.eggert.engineer.task.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.util.Pair;

public final class DateUtil {

  private DateUtil() {}

  /**
   * Helper method for finding the previous period given the selected period
   *
   * @param start - start of the selected period
   * @param end - end of the selected period
   * @return previous period using the diff of start-end from the initial start value
   */
  public static Pair<LocalDate, LocalDate> previousPeriod(LocalDate start, LocalDate end) {
    LocalDate past = start.minusDays(ChronoUnit.DAYS.between(start, end) + 1);
    return Pair.of(past, start.minusDays(1));
  }

  /**
   * Helper method to split a date range into separate ranges
   *
   * @param start - start of the range
   * @param end - end of the range
   * @param stepInDays - should the step be made in a day or a week
   * @return a list of ranges in days/week, depending on the stepInDays param
   */
  public static List<Pair<LocalDate, LocalDate>> splitRange(
      LocalDate start, LocalDate end, boolean stepInDays) {
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
