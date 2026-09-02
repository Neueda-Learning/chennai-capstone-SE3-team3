package com.enterprise.trading.domain.entity;

import com.enterprise.trading.domain.enums.RiskProfile;

import java.time.LocalDate;
import java.util.Objects;

public class Client {

    private final int clientId;
    private final String clientName;
    private final String email;
    private final String passwordHash;
    private final String phoneNo;
    private final LocalDate dateOfBirth;
    private final LocalDate joinDate;
    private final RiskProfile riskProfile;
    private final int advisorId;

    public Client(
            int clientId,
            String clientName,
            String email,
            String passwordHash,
            String phoneNo,
            LocalDate dateOfBirth,
            LocalDate joinDate,
            RiskProfile riskProfile,
            int advisorId) {

        this.clientId = clientId;
        this.clientName = clientName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNo = phoneNo;
        this.dateOfBirth = dateOfBirth;
        this.joinDate = joinDate;
        this.riskProfile = riskProfile;
        this.advisorId = advisorId;
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public RiskProfile getRiskProfile() {
        return riskProfile;
    }

    public int getAdvisorId() {
        return advisorId;
    }
}