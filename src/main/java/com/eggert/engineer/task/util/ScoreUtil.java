package com.eggert.engineer.task.util;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ScoreUtil {

    private static final BigDecimal FIVE = BigDecimal.valueOf(5);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private ScoreUtil() {}

    /**
     * Aggregate percentages using simple average
     * @param percentages - list of percentages
     * @return average percentage
     */
    public static BigDecimal averagePercentage(List<BigDecimal> percentages) {
        if (CollectionUtils.isEmpty(percentages)) {
            return BigDecimal.ZERO;
        }
        return percentages
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(percentages.size()), RoundingMode.FLOOR);
    }

    /**
     * Calculating the score based on category weight and rating
     * @param categoryWeight - weight of the category
     * @param rating - values from 0 to 5
     * @return score in percentage format
     */
    public static BigDecimal calculateScore(BigDecimal categoryWeight, BigDecimal rating) {
        if (isNegativeOrZero(categoryWeight) || isNegativeOrZero(rating)) {
            return BigDecimal.ZERO;
        }
        final BigDecimal ratingPercentage = rating.multiply(ONE_HUNDRED).divide(FIVE, RoundingMode.FLOOR);
        if (isOneHundred(ratingPercentage) && isGreaterThanOne(categoryWeight)) {
            return ratingPercentage;
        }
        return ratingPercentage.multiply(categoryWeight);
    }

    /*
        Helper methods for readability
     */
    private static boolean isGreaterThanOne(BigDecimal number) {
        return number.compareTo(BigDecimal.ONE) > 0;
    }

    private static boolean isOneHundred(BigDecimal number) {
        return number.compareTo(ScoreUtil.ONE_HUNDRED) == 0;
    }

    private static boolean isNegativeOrZero(BigDecimal number) {
        return number.compareTo(BigDecimal.ZERO) < 1;
    }
}
