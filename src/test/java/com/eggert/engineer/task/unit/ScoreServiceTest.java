package com.eggert.engineer.task.unit;

import com.eggert.engineer.grpc.OverallQualityScoreRequest;
import com.eggert.engineer.grpc.OverallQualityScoreResponse;
import com.eggert.engineer.task.ScoreService;
import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.RatingCategory;
import com.eggert.engineer.task.db.entities.Ticket;
import com.eggert.engineer.task.db.repositories.RatingRepository;
import com.eggert.engineer.task.db.repositories.TicketRepository;
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

    private static Rating createRating(int rating) {
        RatingCategory ratingCategory = new RatingCategory();
        ratingCategory.setId(CATEGORY_ID);
        ratingCategory.setName(CATEGORY_NAME);
        ratingCategory.setWeight(CATEGORY_WEIGHT);
        Rating r = new Rating();
        r.setTicket(createTicket());
        r.setRatingCategory(ratingCategory);
        r.setRating(BigDecimal.valueOf(rating));
        return r;
    }

    private static Ticket createTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(TICKET_ID);
        return ticket;
    }
}
