package com.eggert.engineer.task;

import com.eggert.engineer.grpc.CategoryScoresOverPeriodRequest;
import com.eggert.engineer.grpc.OverallQualityScoreRequest;
import com.eggert.engineer.grpc.PeriodOverPeriodScoreChangeRequest;
import com.eggert.engineer.grpc.ScoresByTicketRequest;
import com.google.protobuf.Timestamp;
import java.time.Instant;

public final class ScoreRequestHandler {

  private ScoreRequestHandler() {}

  public static void validateRequest(CategoryScoresOverPeriodRequest request) {
    if (!request.hasPeriodStart()) {
      throw new RuntimeException("Start time is missing");
    }
    if (!request.hasPeriodEnd()) {
      throw new RuntimeException("End time is missing");
    }
    validateDates(request.getPeriodStart(), request.getPeriodEnd());
  }

  public static void validateRequest(OverallQualityScoreRequest request) {
    if (!request.hasPeriodStart()) {
      throw new RuntimeException("Start time is missing");
    }
    if (!request.hasPeriodEnd()) {
      throw new RuntimeException("End time is missing");
    }
    validateDates(request.getPeriodStart(), request.getPeriodEnd());
  }

  public static void validateRequest(ScoresByTicketRequest request) {
    if (!request.hasPeriodStart()) {
      throw new RuntimeException("Start time is missing");
    }
    if (!request.hasPeriodEnd()) {
      throw new RuntimeException("End time is missing");
    }
    validateDates(request.getPeriodStart(), request.getPeriodEnd());
  }

  public static void validateRequest(PeriodOverPeriodScoreChangeRequest request) {
    if (!request.hasSelectedPeriodStart()) {
      throw new RuntimeException("Selected period start time is missing");
    }
    if (!request.hasSelectedPeriodEnd()) {
      throw new RuntimeException("Selected period end time is missing");
    }
    validateDates(request.getSelectedPeriodStart(), request.getSelectedPeriodEnd());
  }

  private static void validateDates(Timestamp periodStart, Timestamp periodEnd) {
    final Instant startTime =
        Instant.ofEpochSecond(periodStart.getSeconds(), periodStart.getNanos());
    final Instant endTime = Instant.ofEpochSecond(periodEnd.getSeconds(), periodEnd.getNanos());
    if (endTime.isBefore(startTime)) {
      throw new RuntimeException("End time cannot be earlier than start time");
    }
  }
}
