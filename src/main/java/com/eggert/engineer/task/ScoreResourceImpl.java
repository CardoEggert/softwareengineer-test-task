package com.eggert.engineer.task;

import com.eggert.engineer.grpc.*;
import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.RatingCategory;
import com.eggert.engineer.task.db.entities.Ticket;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.eggert.engineer.task.util.ScoreUtil.averagePercentageFromRatings;

@GrpcService
public class ScoreResourceImpl extends ScoreResourceGrpc.ScoreResourceImplBase {

    @Autowired
    private ScoreService scoreService;

    // TODO: Date validation
    @Override
    public void categoryScoresOverPeriod(CategoryScoresOverPeriodRequest request, StreamObserver<CategoryScoresOverPeriodResponse> responseObserver) {
        final var responseBuilder = CategoryScoresOverPeriodResponse.newBuilder();
        final LocalDateTime periodStart = convert(request.getPeriodStart());
        final LocalDateTime periodEnd = convert(request.getPeriodEnd());
        final List<Rating> ratings = scoreService.getRatingForPeriod(periodStart, periodEnd);
        final Map<RatingCategory, List<Rating>> ratingsByCategoryMap = ratings.stream().collect(Collectors.groupingBy(Rating::getRatingCategory));
        final long monthsDelta = ChronoUnit.MONTHS.between(periodStart, periodEnd);
        final boolean aggregateDaily = monthsDelta == 0L;
        final List<Pair<LocalDate, LocalDate>> periods = splitRange(periodStart.toLocalDate(), periodEnd.toLocalDate(), aggregateDaily);
        for (Map.Entry<RatingCategory, List<Rating>> ratingsByCategory : ratingsByCategoryMap.entrySet()) {
            final RatingCategory category = ratingsByCategory.getKey();
            final List<Rating> categoryRatings = ratingsByCategory.getValue();
            final var categoryScoreOverPeriodBuilder = CategoryScoreOverPeriod.newBuilder();
            categoryScoreOverPeriodBuilder.setCategory(category.getName());
            categoryScoreOverPeriodBuilder.setRatings(categoryRatings.size());
            categoryScoreOverPeriodBuilder.setScoreForPeriod(averagePercentageFromRatings(categoryRatings).intValue());
            for (Pair<LocalDate, LocalDate> period : periods) {
                final var datePeriodBuilder = PeriodScore.newBuilder();
                datePeriodBuilder.setPeriodStart(period.getFirst().format(DateTimeFormatter.ISO_LOCAL_DATE));
                datePeriodBuilder.setPeriodEnd(period.getSecond().format(DateTimeFormatter.ISO_LOCAL_DATE));
                datePeriodBuilder.setScore(
                        averagePercentageFromRatings(
                                categoryRatings
                                        .stream()
                                        .filter(categoryRating ->
                                                categoryRating.getCreatedAt().isAfter(LocalDateTime.of(period.getFirst(), LocalTime.MIN))
                                                        && categoryRating.getCreatedAt().isBefore(LocalDateTime.of(period.getSecond(), aggregateDaily ? LocalTime.MIN : LocalTime.MAX)))
                                        .toList())
                                .intValue());
                categoryScoreOverPeriodBuilder.addPeriodScores(datePeriodBuilder.build());
            }
            responseBuilder.addCategoryScoreOverPeriods(categoryScoreOverPeriodBuilder.build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    public static List<Pair<LocalDate, LocalDate>> splitRange(LocalDate start, LocalDate end, boolean stepInDays) {
        final List<Pair<LocalDate, LocalDate>> ranges = new ArrayList<>();
        LocalDate currentStart = start;
        while (currentStart.isBefore(end)) {
            LocalDate currentEnd = stepInDays ? currentStart.plusDays(1) : currentStart.plusDays(6);
            if (currentEnd.isAfter(end)) {
                currentEnd = end;
            }
            ranges.add(Pair.of(currentStart, currentEnd));
            currentStart = stepInDays ? currentEnd : currentEnd.plusDays(1);
        }
        return ranges;
    }

    // TODO: Date validation
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
                categoryBuilder.setScore(averagePercentageFromRatings(ratingsForCategory).intValue());
                ticketBuilder.addTicketCategoryScores(categoryBuilder.build());
            }
            responseBuilder.addScoreByTickets(ticketBuilder.build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    // TODO: Date validation
    @Override
    public void overallQualityScore(OverallQualityScoreRequest request, StreamObserver<OverallQualityScoreResponse> responseObserver) {
        final LocalDateTime periodStart = convert(request.getPeriodStart());
        final LocalDateTime periodEnd = convert(request.getPeriodEnd());
        final var responseBuilder = OverallQualityScoreResponse.newBuilder();
        responseBuilder.setScore(scoreService.getScoreForPeriod(periodStart, periodEnd));
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    // TODO: Date validation
    @Override
    public void periodOverPeriodScoreChange(PeriodOverPeriodScoreChangeRequest request, StreamObserver<PeriodOverPeriodScoreChangeResponse> responseObserver) {
        final var responseBuilder = PeriodOverPeriodScoreChangeResponse.newBuilder();
        final LocalDateTime previousPeriodStart = convert(request.getPreviousPeriodStart());
        final LocalDateTime previousPeriodEnd = convert(request.getPreviousPeriodEnd());
        responseBuilder.setPreviousPeriodScore(scoreService.getScoreForPeriod(previousPeriodStart, previousPeriodEnd));
        final LocalDateTime selectedPeriodStart = convert(request.getSelectedPeriodStart());
        final LocalDateTime selectedPeriodEnd = convert(request.getSelectedPeriodEnd());
        responseBuilder.setSelectedPeriodScore(scoreService.getScoreForPeriod(selectedPeriodStart, selectedPeriodEnd));
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    private static LocalDateTime convert(Timestamp timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.getSeconds()), ZoneId.systemDefault());
    }
}
