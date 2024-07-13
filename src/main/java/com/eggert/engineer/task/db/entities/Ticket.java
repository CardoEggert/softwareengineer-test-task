package com.eggert.engineer.task.db.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "subject", nullable = false)
    private String subject;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return id == ticket.id && subject.equals(ticket.subject) && Objects.equals(createdAt, ticket.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subject, createdAt);
    }
}
