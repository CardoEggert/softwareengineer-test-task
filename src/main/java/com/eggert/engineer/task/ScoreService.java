package com.eggert.engineer.task;

import com.eggert.engineer.grpc.*;
import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.RatingCategory;
import com.eggert.engineer.task.db.entities.Ticket;
import com.eggert.engineer.task.db.repositories.RatingRepository;
import com.eggert.engineer.task.db.repositories.TicketRepository;
import com.eggert.engineer.task.util.ScoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.eggert.engineer.task.util.CollectionUtil.batches;
import static com.eggert.engineer.task.util.DateUtil.splitRange;
import static com.eggert.engineer.task.util.ScoreUtil.averagePercentageFromRatings;

@Service
public class ScoreService {

    public static final int BATCH_SIZE = 200;

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private RatingRepository ratingRepository;

    @Transactional(readOnly = true)
    public int getScoreForPeriod(LocalDateTime periodStart, LocalDateTime periodEnd) {
        final List<Rating> ratings = ratingRepository.getRatings(periodStart, periodEnd);
        return ScoreUtil.averagePercentageFromRatings(ratings).intValue();
    }

    @Transactional(readOnly = true)
    public List<ScoreByTicket> getScoresByTicket(LocalDateTime periodStart, LocalDateTime periodEnd) {
        final List<ScoreByTicket> scoreByTickets = new ArrayList<>();
        final List<Ticket> tickets = ticketRepository.getTickets(periodStart, periodEnd);
        for (List<Ticket> batchTickets : batches(tickets, BATCH_SIZE)) {
            final List<Rating> ratings = ratingRepository.getRatingsByTicketIds(
                    batchTickets.stream().map(Ticket::getId).collect(Collectors.toSet()));
            final Map<Integer, List<Rating>> ratingsByTicketId = ratings
                    .stream()
                    .collect(Collectors.groupingBy(x -> x.getTicket().getId()));
            for (Ticket ticket : batchTickets) {
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
                scoreByTickets.add(ticketBuilder.build());
            }
        }
        return scoreByTickets;
    }

    @Transactional(readOnly = true)
    public List<CategoryScoreOverPeriod> getCategoryScoresOverPeriod(LocalDateTime periodStart, LocalDateTime periodEnd) {
        final List<CategoryScoreOverPeriod> scoreOverPeriods = new ArrayList<>();
        final List<Rating> ratings = ratingRepository.getRatings(periodStart, periodEnd);
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
            scoreOverPeriods.add(categoryScoreOverPeriodBuilder.build());
        }
        return scoreOverPeriods;
    }
}
