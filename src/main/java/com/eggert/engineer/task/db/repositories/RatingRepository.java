package com.eggert.engineer.task.db.repositories;

import com.eggert.engineer.task.db.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Integer> {
}
