package com.eggert.engineer.task.unit;

import com.eggert.engineer.grpc.*;
import com.eggert.engineer.task.ScoreService;
import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.RatingCategory;
import com.eggert.engineer.task.db.entities.Ticket;
import com.eggert.engineer.task.db.repositories.RatingRepository;
import com.eggert.engineer.task.db.repositories.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ScoreServiceTest {

    private static final int TICKET_ID = 1;
    private static final int CATEGORY_ID = 2;
    private static final String CATEGORY_NAME = "Klaus";
    private static final BigDecimal CATEGORY_WEIGHT = BigDecimal.valueOf(0.7d);

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private RatingRepository ratingRepository;
    @InjectMocks
    private ScoreService scoreService;

    private static Stream<Arguments> scoreForPeriodSource() {
        return Stream.of(
                Arguments.of(List.of(), 0),
                Arguments.of(List.of(createRating(1)), 14),
                Arguments.of(List.of(createRating(1), createRating(2)), 21),
                Arguments.of(List.of(createRating(1), createRating(2), createRating(3), createRating(4), createRating(5)), 42)
        );
    }

    // TODO: More tests scenarios to add
    @ParameterizedTest
    @MethodSource("scoreForPeriodSource")
    void scoreForPeriod(List<Rating> ratings, int expectedAverage) {
        doReturn(ratings).when(ratingRepository).getRatings(any(), any());
        assertThat(scoreService.getScoreForPeriod(LocalDateTime.MIN, LocalDateTime.MAX)).isEqualTo(expectedAverage);
    }


    // TODO: More in-depth tests to be added here
    @Test
    void categoryScoresOverPeriod_periodInMonths() throws Exception {
        doReturn(List.of(
                createRating(1, LocalDateTime.of(2019, 1, 6,1, 1, 1)),
                createRating(2, LocalDateTime.of(2019, 1, 14,1, 1, 1)),
                createRating(3, LocalDateTime.of(2019, 1, 24,1, 1, 1)),
                createRating(3, LocalDateTime.of(2019, 1, 30,1, 1, 1))))
                .when(ratingRepository)
                .getRatings(any(), any());

        final List<CategoryScoreOverPeriod> results = scoreService.getCategoryScoresOverPeriod(
                LocalDateTime.of(2019, 1, 1,1, 1, 1),
                LocalDateTime.of(2019, 2, 4,1, 1, 1));
        assertThat(results).isNotEmpty();
        final var categoryScoreOverPeriod = results.getFirst();
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
                .when(ratingRepository)
                .getRatings(any(), any());

        final List<CategoryScoreOverPeriod> results = scoreService.getCategoryScoresOverPeriod(
                LocalDateTime.of(2019, 1, 1,1, 1, 1),
                LocalDateTime.of(2019, 1, 4,1, 1, 1));
        assertThat(results).isNotEmpty();
        final var categoryScoreOverPeriod = results.getFirst();
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

    // TODO: More tests to cover scores by tickets in-depth
    @Test
    void scoresByTicket() throws Exception {
        doReturn(List.of(createTicket())).when(ticketRepository).getTickets(any(), any());
        doReturn(List.of(
                createRating(1),
                createRating(2),
                createRating(3)))
                .when(ratingRepository)
                .getRatingsByTicketIds(any());

        final int expectedAverage = 28;
        final List<ScoreByTicket> results = scoreService.getScoresByTicket(LocalDateTime.MIN, LocalDateTime.MAX);
        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().getId()).isNotZero();
        assertThat(results.getFirst().getTicketCategoryScoresList()).isNotEmpty();
        assertThat(results.getFirst().getTicketCategoryScoresList().getFirst().getName()).isNotNull();
        assertThat(results.getFirst().getTicketCategoryScoresList().getFirst().getScore()).isEqualTo(expectedAverage);
    }

    private static Rating createRating(int rating) {
        return createRating(rating, null);
    }

    private static Rating createRating(int rating, LocalDateTime createdAt) {
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

    private static Ticket createTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(TICKET_ID);
        return ticket;
    }
}
