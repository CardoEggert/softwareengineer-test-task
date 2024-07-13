package com.eggert.engineer.task;

import com.eggert.engineer.grpc.*;
import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.RatingCategory;
import com.eggert.engineer.task.db.entities.Ticket;
import com.eggert.engineer.task.util.ScoreUtil;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GrpcService
public class ScoreResourceImpl extends ScoreResourceGrpc.ScoreResourceImplBase {

    @Autowired
    private ScoreService scoreService;

    @Override
    public void scoresByTicket(ScoresByTicketRequest request, StreamObserver<ScoresByTicketResponse> responseObserver) {
        final var responseBuilder = ScoresByTicketResponse.newBuilder();
        final LocalDateTime periodStart = convert(request.getPeriodStart());
        final LocalDateTime periodEnd = convert(request.getPeriodEnd());
        final List<Ticket> tickets =  scoreService.getTicketForPeriod(periodStart, periodEnd);
        final List<Rating> ratings = scoreService.getRatingsForTickets(
                tickets.stream().map(Ticket::getId).collect(Collectors.toSet()));
        final Map<Integer, List<Rating>> ratingsByTicketId = ratings
                .stream()
                .collect(Collectors.groupingBy(x -> x.getTicket().getId()));
        for (Ticket ticket : tickets) {
            final var ticketBuilder = ScoreByTicket.newBuilder();
            ticketBuilder.setId(ticket.getId());
            final List<Rating> ticketRatings = ratingsByTicketId.get(ticket.getId());
            if (CollectionUtils.isEmpty(ticketRatings)) {
                continue;
            }
            final Map<RatingCategory, List<Rating>> ratingsByCategories = ticketRatings
                    .stream()
                    .collect(Collectors.groupingBy(Rating::getRatingCategory));
            for (Map.Entry<RatingCategory, List<Rating>> ratingsByCategory : ratingsByCategories.entrySet()) {
                final var categoryBuilder = TicketCategoryScore.newBuilder();
                final RatingCategory ratingCategory = ratingsByCategory.getKey();
                categoryBuilder.setName(ratingCategory.getName());
                final List<Rating> ratingsForCategory = ratingsByCategory.getValue();
                final List<BigDecimal> scores = ratingsForCategory
                        .stream()
                        .map(x -> ScoreUtil.calculateScore(x.getRatingCategory().getWeight(), x.getRating()))
                        .toList();
                final BigDecimal aggregatedScore = ScoreUtil.averagePercentage(scores);
                categoryBuilder.setScore(aggregatedScore.intValue());
                ticketBuilder.addTicketCategoryScores(categoryBuilder.build());
            }
            responseBuilder.addScoreByTickets(ticketBuilder.build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    private static LocalDateTime convert(Timestamp timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.getSeconds()), ZoneId.systemDefault());
    }
}
