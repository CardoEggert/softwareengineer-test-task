package com.eggert.engineer.task.unit;

import com.eggert.engineer.grpc.OverallQualityScoreRequest;
import com.eggert.engineer.grpc.OverallQualityScoreResponse;
import com.eggert.engineer.grpc.ScoresByTicketRequest;
import com.eggert.engineer.grpc.ScoresByTicketResponse;
import com.eggert.engineer.task.ScoreResourceImpl;
import com.eggert.engineer.task.ScoreService;
import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.RatingCategory;
import com.eggert.engineer.task.db.entities.Ticket;
import com.google.protobuf.Timestamp;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private Ticket createTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(TICKET_ID);
        return ticket;
    }
}
