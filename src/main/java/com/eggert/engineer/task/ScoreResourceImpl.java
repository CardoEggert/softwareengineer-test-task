package com.eggert.engineer.task;

import static com.eggert.engineer.task.ScoreRequestHandler.validateRequest;

import com.eggert.engineer.grpc.*;
import com.eggert.engineer.task.util.DateUtil;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.*;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

@GrpcService
public class ScoreResourceImpl extends ScoreResourceGrpc.ScoreResourceImplBase {

  @Autowired private ScoreService scoreService;

  @Override
  public void categoryScoresOverPeriod(
      CategoryScoresOverPeriodRequest request,
      StreamObserver<CategoryScoresOverPeriodResponse> responseObserver) {
    try {
      validateRequest(request);
    } catch (RuntimeException rte) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription(rte.getMessage()).asException());
    }
    final LocalDateTime periodStart = convert(request.getPeriodStart());
    final LocalDateTime periodEnd = convert(request.getPeriodEnd());
    final CategoryScoresOverPeriodResponse response =
        CategoryScoresOverPeriodResponse.newBuilder()
            .addAllCategoryScoreOverPeriods(
                scoreService.getCategoryScoresOverPeriod(periodStart, periodEnd))
            .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void scoresByTicket(
      ScoresByTicketRequest request, StreamObserver<ScoresByTicketResponse> responseObserver) {
    try {
      validateRequest(request);
    } catch (RuntimeException rte) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription(rte.getMessage()).asException());
    }
    final LocalDateTime periodStart = convert(request.getPeriodStart());
    final LocalDateTime periodEnd = convert(request.getPeriodEnd());
    final ScoresByTicketResponse response =
        ScoresByTicketResponse.newBuilder()
            .addAllScoreByTickets(scoreService.getScoresByTicket(periodStart, periodEnd))
            .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void overallQualityScore(
      OverallQualityScoreRequest request,
      StreamObserver<OverallQualityScoreResponse> responseObserver) {
    try {
      validateRequest(request);
    } catch (RuntimeException rte) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription(rte.getMessage()).asException());
    }
    final LocalDateTime periodStart = convert(request.getPeriodStart());
    final LocalDateTime periodEnd = convert(request.getPeriodEnd());
    final var responseBuilder = OverallQualityScoreResponse.newBuilder();
    responseBuilder.setScore(scoreService.getScoreForPeriod(periodStart, periodEnd));
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }

  @Override
  public void periodOverPeriodScoreChange(
      PeriodOverPeriodScoreChangeRequest request,
      StreamObserver<PeriodOverPeriodScoreChangeResponse> responseObserver) {
    try {
      validateRequest(request);
    } catch (RuntimeException rte) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription(rte.getMessage()).asException());
    }
    final var responseBuilder = PeriodOverPeriodScoreChangeResponse.newBuilder();
    final LocalDateTime selectedPeriodStart = convert(request.getSelectedPeriodStart());
    final LocalDateTime selectedPeriodEnd = convert(request.getSelectedPeriodEnd());
    final Pair<LocalDate, LocalDate> previousPeriod =
        DateUtil.previousPeriod(selectedPeriodStart.toLocalDate(), selectedPeriodEnd.toLocalDate());
    final LocalDateTime previousPeriodStart =
        LocalDateTime.of(previousPeriod.getFirst(), LocalTime.MIN);
    final LocalDateTime previousPeriodEnd =
        LocalDateTime.of(previousPeriod.getSecond(), LocalTime.MAX);
    responseBuilder.setPreviousPeriodScore(
        scoreService.getScoreForPeriod(previousPeriodStart, previousPeriodEnd));
    responseBuilder.setSelectedPeriodScore(
        scoreService.getScoreForPeriod(selectedPeriodStart, selectedPeriodEnd));
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }

  private static LocalDateTime convert(Timestamp timestamp) {
    return LocalDateTime.ofInstant(
        Instant.ofEpochSecond(timestamp.getSeconds()), ZoneId.systemDefault());
  }
}
