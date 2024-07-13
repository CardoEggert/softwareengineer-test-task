package com.eggert.engineer.task.db.repositories;

import com.eggert.engineer.task.db.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
}
