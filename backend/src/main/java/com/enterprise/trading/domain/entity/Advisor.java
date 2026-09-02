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