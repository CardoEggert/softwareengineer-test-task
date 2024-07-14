package com.eggert.engineer.task;

import com.eggert.engineer.grpc.CategoryScoresOverPeriodRequest;
import com.eggert.engineer.grpc.OverallQualityScoreRequest;
import com.eggert.engineer.grpc.PeriodOverPeriodScoreChangeRequest;
import com.eggert.engineer.grpc.ScoresByTicketRequest;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ScoreRequestHandlerTest {

    @Test
    void validateCategoryScoresOverPeriodRequest() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        CategoryScoresOverPeriodRequest.newBuilder().build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        CategoryScoresOverPeriodRequest.newBuilder()
                                .setPeriodStart(Timestamp.newBuilder().build()).build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        CategoryScoresOverPeriodRequest.newBuilder()
                                .setPeriodEnd(Timestamp.newBuilder().build()).build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        CategoryScoresOverPeriodRequest.newBuilder()
                                .setPeriodStart(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                                .setPeriodEnd(Timestamp.newBuilder().setSeconds(Instant.now().minusSeconds(1).getEpochSecond()).build()).build()));
        assertThatNoException().isThrownBy(() -> ScoreRequestHandler.validateRequest(
                CategoryScoresOverPeriodRequest.newBuilder()
                        .setPeriodStart(Timestamp.newBuilder().build())
                        .setPeriodEnd(Timestamp.newBuilder().build()).build()));
    }

    @Test
    void validateOverallQualityScoreRequest() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        OverallQualityScoreRequest.newBuilder().build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        OverallQualityScoreRequest.newBuilder()
                                .setPeriodStart(Timestamp.newBuilder().build()).build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        OverallQualityScoreRequest.newBuilder()
                                .setPeriodEnd(Timestamp.newBuilder().build()).build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        OverallQualityScoreRequest.newBuilder()
                                .setPeriodStart(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                                .setPeriodEnd(Timestamp.newBuilder().setSeconds(Instant.now().minusSeconds(1).getEpochSecond()).build()).build()));
        assertThatNoException().isThrownBy(() -> ScoreRequestHandler.validateRequest(
                OverallQualityScoreRequest.newBuilder()
                        .setPeriodStart(Timestamp.newBuilder().build())
                        .setPeriodEnd(Timestamp.newBuilder().build()).build()));
    }

    @Test
    void validateScoresByTicketRequest() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        ScoresByTicketRequest.newBuilder().build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        ScoresByTicketRequest.newBuilder()
                                .setPeriodStart(Timestamp.newBuilder().build()).build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        ScoresByTicketRequest.newBuilder()
                                .setPeriodEnd(Timestamp.newBuilder().build()).build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        ScoresByTicketRequest.newBuilder()
                                .setPeriodStart(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                                .setPeriodEnd(Timestamp.newBuilder().setSeconds(Instant.now().minusSeconds(1).getEpochSecond()).build()).build()));
        assertThatNoException().isThrownBy(() -> ScoreRequestHandler.validateRequest(
                ScoresByTicketRequest.newBuilder()
                        .setPeriodStart(Timestamp.newBuilder().build())
                        .setPeriodEnd(Timestamp.newBuilder().build()).build()));
    }

    @Test
    void validatePeriodOverPeriodScoreChangeRequest() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        PeriodOverPeriodScoreChangeRequest.newBuilder().build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        PeriodOverPeriodScoreChangeRequest.newBuilder()
                                .setSelectedPeriodStart(Timestamp.newBuilder().build()).build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        PeriodOverPeriodScoreChangeRequest.newBuilder()
                                .setSelectedPeriodEnd(Timestamp.newBuilder().build()).build()));
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ScoreRequestHandler.validateRequest(
                        PeriodOverPeriodScoreChangeRequest.newBuilder()
                                .setSelectedPeriodStart(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                                .setSelectedPeriodEnd(Timestamp.newBuilder().setSeconds(Instant.now().minusSeconds(1).getEpochSecond()).build()).build()));
        assertThatNoException().isThrownBy(() -> ScoreRequestHandler.validateRequest(
                PeriodOverPeriodScoreChangeRequest.newBuilder()
                        .setSelectedPeriodStart(Timestamp.newBuilder().build())
                        .setSelectedPeriodEnd(Timestamp.newBuilder().build()).build()));
    }
}