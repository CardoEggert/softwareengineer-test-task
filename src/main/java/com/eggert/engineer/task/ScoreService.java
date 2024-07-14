package com.eggert.engineer.task;

import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.Ticket;
import com.eggert.engineer.task.db.repositories.RatingRepository;
import com.eggert.engineer.task.db.repositories.TicketRepository;
import com.eggert.engineer.task.util.ScoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ScoreService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private RatingRepository ratingRepository;

    public List<Ticket> getTicketForPeriod(LocalDateTime periodStart, LocalDateTime periodEnd) {
        return ticketRepository.getTickets(periodStart, periodEnd);
    }

    @Transactional(readOnly = true) // Transactional needed for keeping the session open for fetching the foreign table values
    public List<Rating> getRatingsForTickets(Set<Integer> ticketIds) {
        return ratingRepository.getRatingsByTicketIds(ticketIds);
    }

    @Transactional(readOnly = true)
    public int getScoreForPeriod(LocalDateTime periodStart, LocalDateTime periodEnd) {
        final List<Rating> ratings = ratingRepository.getRatings(periodStart, periodEnd);
        return ScoreUtil.averagePercentageFromRatings(ratings).intValue();
    }

    @Transactional(readOnly = true)
    public List<Rating> getRatingForPeriod(LocalDateTime periodStart, LocalDateTime periodEnd) {
        return ratingRepository.getRatings(periodStart, periodEnd);
    }

}
