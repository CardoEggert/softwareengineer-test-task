package com.eggert.engineer.task.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eggert.engineer.grpc.*;
import com.eggert.engineer.task.ScoreResourceImpl;
import com.eggert.engineer.task.ScoreService;
import com.google.protobuf.Timestamp;
import io.grpc.internal.testing.StreamRecorder;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ScoreResourceImplTest {

  private static final Timestamp PERIOD_START_TS =
      Timestamp.newBuilder()
          .setSeconds(LocalDateTime.of(2019, 3, 1, 1, 1, 1).toEpochSecond(ZoneOffset.UTC))
          .build();
  private static final Timestamp PERIOD_END_TS =
      Timestamp.newBuilder()
          .setSeconds(LocalDateTime.of(2019, 4, 1, 1, 1, 1).toEpochSecond(ZoneOffset.UTC))
          .build();
  private static final LocalDateTime PERIOD_START = convert(PERIOD_START_TS);
  private static final LocalDateTime PERIOD_END = convert(PERIOD_END_TS);

  @Mock private ScoreService scoreService;
  @InjectMocks private ScoreResourceImpl scoreResource;

  @Test
  void categoryScoresOverPeriod() throws Exception {
    final List<CategoryScoreOverPeriod> expectedList = new ArrayList<>();
    expectedList.add(
        CategoryScoreOverPeriod.newBuilder()
            .setCategory("abc")
            .setRatings(2)
            .setScoreForPeriod(20)
            .addPeriodScores(
                PeriodScore.newBuilder()
                    .setScore(20)
                    .setPeriodStart("abc")
                    .setPeriodEnd("bcd")
                    .build())
            .build());
    doReturn(expectedList).when(scoreService).getCategoryScoresOverPeriod(any(), any());

    CategoryScoresOverPeriodRequest request =
        CategoryScoresOverPeriodRequest.newBuilder()
            .setPeriodStart(PERIOD_START_TS)
            .setPeriodEnd(PERIOD_END_TS)
            .build();
    StreamRecorder<CategoryScoresOverPeriodResponse> responseObserver = StreamRecorder.create();
    scoreResource.categoryScoresOverPeriod(request, responseObserver);
    if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
      fail("The call did not terminate in time");
    }
    assertNull(responseObserver.getError());
    final var results = responseObserver.getValues();
    assertThat(results.getFirst().getCategoryScoreOverPeriodsList()).isEqualTo(expectedList);
    verify(scoreService).getCategoryScoresOverPeriod(PERIOD_START, PERIOD_END);
  }

  @Test
  void scoresByTicket() throws Exception {
    final List<ScoreByTicket> expectedList = new ArrayList<>();
    expectedList.add(
        ScoreByTicket.newBuilder()
            .setId(123)
            .addTicketCategoryScores(
                TicketCategoryScore.newBuilder().setName("abc").setScore(42).build())
            .build());
    doReturn(expectedList).when(scoreService).getScoresByTicket(any(), any());
    ScoresByTicketRequest request =
        ScoresByTicketRequest.newBuilder()
            .setPeriodStart(PERIOD_START_TS)
            .setPeriodEnd(PERIOD_END_TS)
            .build();
    StreamRecorder<ScoresByTicketResponse> responseObserver = StreamRecorder.create();
    scoreResource.scoresByTicket(request, responseObserver);
    if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
      fail("The call did not terminate in time");
    }
    assertNull(responseObserver.getError());
    final var results = responseObserver.getValues();
    assertThat(results).isNotEmpty();
    assertThat(results.getFirst().getScoreByTicketsList()).isEqualTo(expectedList);
    verify(scoreService).getScoresByTicket(PERIOD_START, PERIOD_END);
  }

  @Captor ArgumentCaptor<LocalDateTime> periodStartCaptor;
  @Captor ArgumentCaptor<LocalDateTime> periodEndCaptor;

  @Test
  void periodOverPeriodScoreChange() throws Exception {
    final int expectedScore = 42;
    doReturn(expectedScore).when(scoreService).getScoreForPeriod(any(), any());
    PeriodOverPeriodScoreChangeRequest request =
        PeriodOverPeriodScoreChangeRequest.newBuilder()
            .setSelectedPeriodStart(PERIOD_START_TS)
            .setSelectedPeriodEnd(PERIOD_END_TS)
            .build();
    StreamRecorder<PeriodOverPeriodScoreChangeResponse> responseObserver = StreamRecorder.create();
    scoreResource.periodOverPeriodScoreChange(request, responseObserver);
    if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
      fail("The call did not terminate in time");
    }
    assertNull(responseObserver.getError());
    final var result = responseObserver.getValues();
    assertThat(result).isNotEmpty();
    assertThat(result.getFirst().getPreviousPeriodScore()).isEqualTo(expectedScore);
    assertThat(result.getFirst().getSelectedPeriodScore()).isEqualTo(expectedScore);
    verify(scoreService, times(2))
        .getScoreForPeriod(periodStartCaptor.capture(), periodEndCaptor.capture());
    assertThat(periodStartCaptor.getAllValues())
        .anySatisfy(
            periodStart -> {
              assertThat(periodStart).isEqualTo(PERIOD_START);
            })
        .anySatisfy(
            periodStart -> {
              assertThat(periodStart).isNotEqualTo(PERIOD_START);
            });
    assertThat(periodEndCaptor.getAllValues())
        .anySatisfy(
            periodEnd -> {
              assertThat(periodEnd).isEqualTo(PERIOD_END);
            })
        .anySatisfy(
            periodEnd -> {
              assertThat(periodEnd).isNotEqualTo(PERIOD_END);
            });
  }

  @Test
  void overallQualityScore() throws Exception {
    final int expectedAverage = 42;
    doReturn(expectedAverage).when(scoreService).getScoreForPeriod(any(), any());

    OverallQualityScoreRequest request =
        OverallQualityScoreRequest.newBuilder()
            .setPeriodStart(Timestamp.newBuilder().build())
            .setPeriodEnd(Timestamp.newBuilder().build())
            .build();
    StreamRecorder<OverallQualityScoreResponse> responseObserver = StreamRecorder.create();
    scoreResource.overallQualityScore(request, responseObserver);
    if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
      fail("The call did not terminate in time");
    }
    assertNull(responseObserver.getError());
    final var result = responseObserver.getValues();
    assertThat(result.getFirst().getScore()).isEqualTo(expectedAverage);

    verify(scoreService).getScoreForPeriod(any(), any());
  }

  private static LocalDateTime convert(Timestamp timestamp) {
    return LocalDateTime.ofInstant(
        Instant.ofEpochSecond(timestamp.getSeconds()), ZoneId.systemDefault());
  }
}
