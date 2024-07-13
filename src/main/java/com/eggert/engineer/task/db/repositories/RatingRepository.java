package com.eggert.engineer.task.db.repositories;

import com.eggert.engineer.task.db.entities.Rating;
import com.eggert.engineer.task.db.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    @Query("SELECT r FROM Rating r WHERE r.createdAt BETWEEN :periodStart AND :periodEnd")
    List<Rating> getRatings(@Param("periodStart") LocalDateTime periodStart, @Param("periodEnd") LocalDateTime periodEnd);

}
