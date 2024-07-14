package com.eggert.engineer.task.unit;

import com.eggert.engineer.grpc.*;
import com.eggert.engineer.task.ScoreResourceImpl;
import com.eggert.engineer.task.ScoreService;
import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.RatingCategory;
import com.eggert.engineer.task.db.entities.Ticket;
import com.google.protobuf.Timestamp;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ScoreResourceImplTest {

    private static final int TICKET_ID = 1;
    private static final int CATEGORY_ID = 2;
    private static final String CATEGORY_NAME = "Klaus";
    private static final BigDecimal CATEGORY_WEIGHT = BigDecimal.valueOf(0.7d);

    @Mock
    private ScoreService scoreService;
    @InjectMocks
    private ScoreResourceImpl scoreResource;

    @Test
    void categoryScoresOverPeriod_periodInMonths() throws Exception {
        doReturn(List.of(
                createRating(1, LocalDateTime.of(2019, 1, 6,1, 1, 1)),
                createRating(2, LocalDateTime.of(2019, 1, 14,1, 1, 1)),
                createRating(3, LocalDateTime.of(2019, 1, 24,1, 1, 1)),
                createRating(3, LocalDateTime.of(2019, 1, 30,1, 1, 1))))
                .when(scoreService)
                .getRatingForPeriod(any(), any());

        CategoryScoresOverPeriodRequest request = CategoryScoresOverPeriodRequest.newBuilder()
                .setPeriodStart(Timestamp.newBuilder().setSeconds(LocalDateTime.of(2019, 1, 1, 1, 1, 1).toEpochSecond(ZoneOffset.UTC)).build())
                .setPeriodEnd(Timestamp.newBuilder().setSeconds(LocalDateTime.of(2019, 2, 4, 1, 1, 1).toEpochSecond(ZoneOffset.UTC)).build())
                .build();
        StreamRecorder<CategoryScoresOverPeriodResponse> responseObserver = StreamRecorder.create();
        scoreResource.categoryScoresOverPeriod(request, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }
        assertNull(responseObserver.getError());
        final var results = responseObserver.getValues();
        assertThat(results).isNotEmpty();
        final var categoryScoreOverPeriod = results.getFirst().getCategoryScoreOverPeriodsList().getFirst();
        assertThat(categoryScoreOverPeriod.getCategory()).isEqualTo(CATEGORY_NAME);
        assertThat(categoryScoreOverPeriod.getScoreForPeriod()).isEqualTo(31);
        assertThat(categoryScoreOverPeriod.getRatings()).isEqualTo(4);
        assertThat(categoryScoreOverPeriod.getPeriodScoresList()).hasSize(5);
        assertThat(categoryScoreOverPeriod.getPeriodScoresList())
                .anySatisfy(periodScore -> {
            assertThat(periodScore.getPeriodStart()).isEqualTo("2019-01-01");
            assertThat(periodScore.getPeriodEnd()).isEqualTo("2019-01-07");
            assertThat(periodScore.getScore()).isEqualTo(14);
        }).anySatisfy(periodScore -> {
            assertThat(periodScore.getPeriodStart()).isEqualTo("2019-01-08");
            assertThat(periodScore.getPeriodEnd()).isEqualTo("2019-01-14");
            assertThat(periodScore.getScore()).isEqualTo(28);
        }).anySatisfy(periodScore -> {
            assertThat(periodScore.getPeriodStart()).isEqualTo("2019-01-15");
            assertThat(periodScore.getPeriodEnd()).isEqualTo("2019-01-21");
            assertThat(periodScore.getScore()).isZero();
        }).anySatisfy(periodScore -> {
            assertThat(periodScore.getPeriodStart()).isEqualTo("2019-01-22");
            assertThat(periodScore.getPeriodEnd()).isEqualTo("2019-01-28");
            assertThat(periodScore.getScore()).isEqualTo(42);
        }).anySatisfy(periodScore -> {
            assertThat(periodScore.getPeriodStart()).isEqualTo("2019-01-29");
            assertThat(periodScore.getPeriodEnd()).isEqualTo("2019-02-04");
            assertThat(periodScore.getScore()).isEqualTo(42);
        });
    }
    @Test
    void categoryScoresOverPeriod_periodInDays() throws Exception {
        doReturn(List.of(
                createRating(1, LocalDateTime.of(2019, 1, 1,1, 1, 1)),
                createRating(2, LocalDateTime.of(2019, 1, 2,1, 1, 1)),
                createRating(3, LocalDateTime.of(2019, 1, 3,1, 1, 1))))
                .when(scoreService)
                .getRatingForPeriod(any(), any());

        CategoryScoresOverPeriodRequest request = CategoryScoresOverPeriodRequest.newBuilder()
                .setPeriodStart(Timestamp.newBuilder().setSeconds(LocalDateTime.of(2019, 1, 1, 1, 1, 1).toEpochSecond(ZoneOffset.UTC)).build())
                .setPeriodEnd(Timestamp.newBuilder().setSeconds(LocalDateTime.of(2019, 1, 4, 1, 1, 1).toEpochSecond(ZoneOffset.UTC)).build())
                .build();
        StreamRecorder<CategoryScoresOverPeriodResponse> responseObserver = StreamRecorder.create();
        scoreResource.categoryScoresOverPeriod(request, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }
        assertNull(responseObserver.getError());
        final var results = responseObserver.getValues();
        assertThat(results).isNotEmpty();
        final var categoryScoreOverPeriod = results.getFirst().getCategoryScoreOverPeriodsList().getFirst();
        assertThat(categoryScoreOverPeriod.getCategory()).isEqualTo(CATEGORY_NAME);
        assertThat(categoryScoreOverPeriod.getScoreForPeriod()).isEqualTo(28);
        assertThat(categoryScoreOverPeriod.getRatings()).isEqualTo(3);
        assertThat(categoryScoreOverPeriod.getPeriodScoresList()).hasSize(3);
        assertThat(categoryScoreOverPeriod.getPeriodScoresList())
                .anySatisfy(periodScore -> {
            assertThat(periodScore.getPeriodStart()).isEqualTo("2019-01-01");
            assertThat(periodScore.getPeriodEnd()).isEqualTo("2019-01-02");
            assertThat(periodScore.getScore()).isEqualTo(14);
        }).anySatisfy(periodScore -> {
            assertThat(periodScore.getPeriodStart()).isEqualTo("2019-01-02");
            assertThat(periodScore.getPeriodEnd()).isEqualTo("2019-01-03");
            assertThat(periodScore.getScore()).isEqualTo(28);
        }).anySatisfy(periodScore -> {
            assertThat(periodScore.getPeriodStart()).isEqualTo("2019-01-03");
            assertThat(periodScore.getPeriodEnd()).isEqualTo("2019-01-04");
            assertThat(periodScore.getScore()).isEqualTo(42);
        });
    }

    // TODO: More tests
    @Test
    void scoresByTicket() throws Exception {
        doReturn(List.of(createTicket())).when(scoreService).getTicketForPeriod(any(), any());
        doReturn(List.of(
                createRating(1),
                createRating(2),
                createRating(3)))
                .when(scoreService)
                .getRatingsForTickets(any());

        final int expectedAverage = 28;
        ScoresByTicketRequest request = ScoresByTicketRequest.newBuilder()
                .setPeriodStart(Timestamp.newBuilder().build())
                .setPeriodEnd(Timestamp.newBuilder().build())
                .build();
        StreamRecorder<ScoresByTicketResponse> responseObserver = StreamRecorder.create();
        scoreResource.scoresByTicket(request, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }
        assertNull(responseObserver.getError());
        final var results = responseObserver.getValues();
        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().getScoreByTicketsList()).isNotEmpty();
        assertThat(results.getFirst().getScoreByTicketsList().getFirst().getId()).isNotNull();
        assertThat(results.getFirst().getScoreByTicketsList().getFirst().getTicketCategoryScoresList()).isNotEmpty();
        assertThat(results.getFirst().getScoreByTicketsList().getFirst().getTicketCategoryScoresList().getFirst().getName()).isNotNull();
        assertThat(results.getFirst().getScoreByTicketsList().getFirst().getTicketCategoryScoresList().getFirst().getScore()).isEqualTo(expectedAverage);
    }

    @Test
    void overallQualityScore() throws Exception {
        final int expectedAverage = 42;
        doReturn(expectedAverage).when(scoreService).getScoreForPeriod(any(), any());

        OverallQualityScoreRequest request = OverallQualityScoreRequest.newBuilder()
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

    private Rating createRating(int rating) {
        return createRating(rating, null);
    }

    private Rating createRating(int rating, LocalDateTime createdAt) {
        RatingCategory ratingCategory = new RatingCategory();
        ratingCategory.setId(CATEGORY_ID);
        ratingCategory.setName(CATEGORY_NAME);
        ratingCategory.setWeight(CATEGORY_WEIGHT);
        Rating r = new Rating();
        r.setTicket(createTicket());
        r.setRatingCategory(ratingCategory);
        r.setRating(BigDecimal.valueOf(rating));
        r.setCreatedAt(createdAt);
        return r;
    }

    private Ticket createTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(TICKET_ID);
        return ticket;
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
        assertThat(ScoreResourceImpl.splitRange(start, end, stepInDays)).isEqualTo(ranges);
    }
}
