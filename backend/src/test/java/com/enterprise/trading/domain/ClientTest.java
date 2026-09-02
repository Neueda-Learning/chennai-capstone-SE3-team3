package com.enterprise.trading.domain;

import com.enterprise.trading.domain.entity.Client;
import com.enterprise.trading.domain.enums.RiskProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Client")
class ClientTest {

    private Client createValidClient() {
        return new Client(
                1,
                "Aviral Singh",
                "aviral@example.com",
                "hashedPassword123",
                "9876543210",
                LocalDate.of(1999, 5, 10),
                LocalDate.of(2026, 9, 2),
                RiskProfile.MEDIUM,
                1
        );
    }

    @Test
    @DisplayName("stores the client details")
    void storesClientDetails() {

        Client client = createValidClient();

        assertEquals(1, client.getClientId());
        assertEquals("Aviral Singh", client.getClientName());
        assertEquals("aviral@example.com", client.getEmail());
        assertEquals("hashedPassword123", client.getPasswordHash());
        assertEquals("9876543210", client.getPhoneNo());
        assertEquals(
                LocalDate.of(1999, 5, 10),
                client.getDateOfBirth()
        );
        assertEquals(
                LocalDate.of(2026, 9, 2),
                client.getJoinDate()
        );
        assertEquals(RiskProfile.MEDIUM, client.getRiskProfile());
        assertEquals(1, client.getAdvisorId());
    }

    @Test
    @DisplayName("accepts the minimum valid client ID")
    void acceptsMinimumValidClientId() {

        Client client = new Client(
                1,
                "Aviral Singh",
                "aviral@example.com",
                "hashedPassword123",
                "9876543210",
                LocalDate.of(1999, 5, 10),
                LocalDate.of(2026, 9, 2),
                RiskProfile.MEDIUM,
                1
        );

        assertEquals(1, client.getClientId());
    }

    @Test
    @DisplayName("rejects a client ID of zero")
    void rejectsZeroClientId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        0,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a negative client ID")
    void rejectsNegativeClientId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        -1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a null client name")
    void rejectsNullClientName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        null,
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a blank client name")
    void rejectsBlankClientName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "   ",
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a null email")
    void rejectsNullEmail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        null,
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a blank email")
    void rejectsBlankEmail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "   ",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a null password hash")
    void rejectsNullPasswordHash() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        null,
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a blank password hash")
    void rejectsBlankPasswordHash() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "   ",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a null phone number")
    void rejectsNullPhoneNumber() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        null,
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a blank phone number")
    void rejectsBlankPhoneNumber() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        "   ",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a null date of birth")
    void rejectsNullDateOfBirth() {

        assertThrows(
                NullPointerException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        null,
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a null join date")
    void rejectsNullJoinDate() {

        assertThrows(
                NullPointerException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        null,
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects a join date before date of birth")
    void rejectsJoinDateBeforeDateOfBirth() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(2026, 9, 2),
                        LocalDate.of(2020, 1, 1),
                        RiskProfile.MEDIUM,
                        1
                )
        );
    }

    @Test
    @DisplayName("accepts a join date equal to date of birth")
    void acceptsJoinDateEqualToDateOfBirth() {

        LocalDate date = LocalDate.of(2026, 9, 2);

        Client client = new Client(
                1,
                "Aviral Singh",
                "aviral@example.com",
                "hashedPassword123",
                "9876543210",
                date,
                date,
                RiskProfile.MEDIUM,
                1
        );

        assertEquals(date, client.getJoinDate());
    }

    @Test
    @DisplayName("rejects a null risk profile")
    void rejectsNullRiskProfile() {

        assertThrows(
                NullPointerException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        null,
                        1
                )
        );
    }

    @Test
    @DisplayName("rejects an advisor ID of zero")
    void rejectsZeroAdvisorId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        0
                )
        );
    }

    @Test
    @DisplayName("rejects a negative advisor ID")
    void rejectsNegativeAdvisorId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Client(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        "9876543210",
                        LocalDate.of(1999, 5, 10),
                        LocalDate.of(2026, 9, 2),
                        RiskProfile.MEDIUM,
                        -1
                )
        );
    }

    @Test
    @DisplayName("preserves the supplied risk profile")
    void preservesRiskProfile() {

        Client client = createValidClient();

        assertEquals(
                RiskProfile.MEDIUM,
                client.getRiskProfile()
        );
    }

    @Test
    @DisplayName("preserves the supplied phone number")
    void preservesPhoneNumber() {

        Client client = createValidClient();

        assertEquals(
                "9876543210",
                client.getPhoneNo()
        );
    }

    @Test
    @DisplayName("preserves the supplied advisor ID")
    void preservesAdvisorId() {

        Client client = createValidClient();

        assertEquals(1, client.getAdvisorId());
    }
}

