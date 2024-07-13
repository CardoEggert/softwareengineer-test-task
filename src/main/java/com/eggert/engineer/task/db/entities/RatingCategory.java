package com.eggert.engineer.task.db.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "rating_categories")
public class RatingCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "weight", nullable = false)
    private BigDecimal weight;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatingCategory that = (RatingCategory) o;
        return id == that.id && name.equals(that.name) && weight.equals(that.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, weight);
    }
}
