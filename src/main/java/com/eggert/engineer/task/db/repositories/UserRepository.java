package com.eggert.engineer.task.db.repositories;

import com.eggert.engineer.task.db.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {}
