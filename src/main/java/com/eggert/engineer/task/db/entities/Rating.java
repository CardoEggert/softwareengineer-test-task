package com.eggert.engineer.task.db.entities;

import com.eggert.engineer.task.db.converter.LocalDateTimeAttributeConverter;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "ratings")
public class Rating {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "rating", nullable = false)
  private BigDecimal rating;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "ticket_id")
  private Ticket ticket;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "rating_category_id")
  private RatingCategory ratingCategory;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewer_id")
  private User reviewer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewee_id")
  private User reviewee;

  @Column(name = "created_at")
  @Convert(converter = LocalDateTimeAttributeConverter.class)
  private LocalDateTime createdAt;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public BigDecimal getRating() {
    return rating;
  }

  public void setRating(BigDecimal rating) {
    this.rating = rating;
  }

  public Ticket getTicket() {
    return ticket;
  }

  public void setTicket(Ticket ticket) {
    this.ticket = ticket;
  }

  public RatingCategory getRatingCategory() {
    return ratingCategory;
  }

  public void setRatingCategory(RatingCategory ratingCategory) {
    this.ratingCategory = ratingCategory;
  }

  public User getReviewer() {
    return reviewer;
  }

  public User getReviewee() {
    return reviewee;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Rating rating1 = (Rating) o;
    return id == rating1.id
        && rating.equals(rating1.rating)
        && ticket.equals(rating1.ticket)
        && ratingCategory.equals(rating1.ratingCategory)
        && reviewer.equals(rating1.reviewer)
        && reviewee.equals(rating1.reviewee)
        && Objects.equals(createdAt, rating1.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, rating, ticket, ratingCategory, reviewer, reviewee, createdAt);
  }
}
