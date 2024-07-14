package com.eggert.engineer.task.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.eggert.engineer.grpc.*;
import com.google.protobuf.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    properties = {
      "grpc.server.inProcessName=test", // Enable inProcess server
      "grpc.server.port=-1", // Disable external server
      "grpc.client.inProcess.address=in-process:test" // Configure the client to connect to the
      // inProcess server
    })
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ScoreResourceIT {

  private static final Timestamp PERIOD_START =
      Timestamp.newBuilder()
          .setSeconds(LocalDateTime.of(2019, 3, 1, 1, 1, 1).toEpochSecond(ZoneOffset.UTC))
          .build();
  private static final Timestamp PERIOD_END =
      Timestamp.newBuilder()
          .setSeconds(LocalDateTime.of(2019, 4, 1, 1, 1, 1).toEpochSecond(ZoneOffset.UTC))
          .build();

  @GrpcClient("inProcess")
  private ScoreResourceGrpc.ScoreResourceBlockingStub blockingStub;

  /*
  TODO: Ideally there should be better integration tests that generate the data and then assert that we get the expected result
  Right now as we are using a fixed database, then I do not see any issue with just having some certain values being used for it
  + There should be integration tests that try to cover failing scenarios as well such as conflicting periods or even missing ones
   */

  @Test
  public void categoryScoresOverPeriod() {
    final CategoryScoresOverPeriodRequest request =
        CategoryScoresOverPeriodRequest.newBuilder()
            .setPeriodStart(PERIOD_START)
            .setPeriodEnd(PERIOD_END)
            .build();

    final CategoryScoresOverPeriodResponse response =
        blockingStub.categoryScoresOverPeriod(request);
    assertThat(response).isNotNull();
    assertThat(response.getCategoryScoreOverPeriodsList())
        .isNotEmpty()
        .anySatisfy(
            categoryScoreOverPeriod -> {
              assertThat(categoryScoreOverPeriod.getCategory()).isNotEmpty();
              assertThat(categoryScoreOverPeriod.getRatings()).isNotZero();
              assertThat(categoryScoreOverPeriod.getScoreForPeriod()).isNotZero();
              assertThat(categoryScoreOverPeriod.getPeriodScoresList())
                  .isNotEmpty()
                  .anySatisfy(
                      periodScore -> {
                        assertThat(periodScore.getPeriodStart()).isNotEmpty();
                        assertThat(periodScore.getPeriodEnd()).isNotEmpty();
                        assertThat(periodScore.getScore()).isNotZero();
                      });
            });
  }

  @Test
  public void scoresByTicket() {
    final ScoresByTicketRequest request =
        ScoresByTicketRequest.newBuilder()
            .setPeriodStart(PERIOD_START)
            .setPeriodEnd(PERIOD_END)
            .build();

    final ScoresByTicketResponse response = blockingStub.scoresByTicket(request);
    assertThat(response).isNotNull();
    assertThat(response.getScoreByTicketsList())
        .isNotEmpty()
        .anySatisfy(
            scoreByTicket -> {
              assertThat(scoreByTicket.getId()).isNotZero();
              assertThat(scoreByTicket.getTicketCategoryScoresList())
                  .isNotEmpty()
                  .anySatisfy(
                      ticketCategoryScore -> {
                        assertThat(ticketCategoryScore.getName()).isNotEmpty();
                        assertThat(ticketCategoryScore.getScore()).isNotZero();
                      });
            });
  }

  @Test
  public void overallQualityScore() {
    final OverallQualityScoreRequest request =
        OverallQualityScoreRequest.newBuilder()
            .setPeriodStart(PERIOD_START)
            .setPeriodEnd(PERIOD_END)
            .build();

    assertThat(blockingStub.overallQualityScore(request).getScore()).isEqualTo(35);
  }

  @Test
  public void periodOverPeriodScoreChange() {
    final PeriodOverPeriodScoreChangeRequest request =
        PeriodOverPeriodScoreChangeRequest.newBuilder()
            .setSelectedPeriodStart(PERIOD_START)
            .setSelectedPeriodEnd(PERIOD_END)
            .build();

    final PeriodOverPeriodScoreChangeResponse response =
        blockingStub.periodOverPeriodScoreChange(request);
    assertThat(response).isNotNull();
    assertThat(response.getPreviousPeriodScore()).isNotZero();
    assertThat(response.getSelectedPeriodScore()).isNotZero();
  }
}
