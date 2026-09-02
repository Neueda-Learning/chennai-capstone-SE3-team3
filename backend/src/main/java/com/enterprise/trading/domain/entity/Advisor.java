package com.enterprise.trading.domain.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Advisor {

    private final int advisorId;
    private final String advisorName;
    private final String email;
    private final String passwordHash;
    private final LocalDate hireDate;

    public Advisor(
            int advisorId,
            String advisorName,
            String email,
            String passwordHash,
            LocalDate hireDate) {

        if (advisorId < 1) {
            throw new IllegalArgumentException(
                    "Advisor ID must be at least 1");
        }

        if (advisorName == null || advisorName.isBlank()) {
            throw new IllegalArgumentException(
                    "Advisor name is required");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash is required");
        }

        Objects.requireNonNull(
                hireDate,
                "Hire date is required");

        this.advisorId = advisorId;
        this.advisorName = advisorName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.hireDate = hireDate;
    }
    public int getAdvisorId() {
        return advisorId;
    }

    public String getAdvisorName() {
        return advisorName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }
}