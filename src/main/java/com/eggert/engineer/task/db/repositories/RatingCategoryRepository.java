package com.eggert.engineer.task.db.repositories;

import com.eggert.engineer.task.db.entities.RatingCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingCategoryRepository extends JpaRepository<RatingCategory, Integer> {}
