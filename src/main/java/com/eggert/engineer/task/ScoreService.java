package com.eggert.engineer.task;

import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.Ticket;
import com.eggert.engineer.task.db.repositories.RatingRepository;
import com.eggert.engineer.task.db.repositories.TicketRepository;
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

    // TODO: Needs a solution with batching, as we are currently using SQLite then we can just load everything to memory and filter it
    @Transactional(readOnly= true) // Transactional needed for keeping the session open for fetching the foreign table values
    public List<Rating> getRatingsForTickets(Set<Integer> ticketIds) {
        return ratingRepository.findAll().stream().filter(rating -> ticketIds.contains(rating.getTicket().getId())).toList();
    }

}
