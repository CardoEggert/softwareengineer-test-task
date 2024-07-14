package com.eggert.engineer.task.db.repositories;

import com.eggert.engineer.task.db.entities.Ticket;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

  @Query("SELECT t FROM Ticket t WHERE t.createdAt BETWEEN :periodStart AND :periodEnd")
  List<Ticket> getTickets(
      @Param("periodStart") LocalDateTime periodStart, @Param("periodEnd") LocalDateTime periodEnd);
}
