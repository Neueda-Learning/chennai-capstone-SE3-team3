package com.enterprise.trading.domain;

import com.enterprise.trading.domain.entity.Advisor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Advisor")
class AdvisorTest {

    @Test
    @DisplayName("stores the advisor details")
    void storesAdvisorDetails() {

        Advisor advisor = new Advisor(
                1,
                "Aviral Singh",
                "aviral@example.com",
                "hashedPassword123",
                LocalDate.of(2026, 9, 2)
        );

        assertEquals(1, advisor.getAdvisorId());
        assertEquals("Aviral Singh", advisor.getAdvisorName());
        assertEquals("aviral@example.com", advisor.getEmail());
        assertEquals("hashedPassword123", advisor.getPasswordHash());
        assertEquals(
                LocalDate.of(2026, 9, 2),
                advisor.getHireDate()
        );
    }

    @Test
    @DisplayName("accepts the minimum valid advisor ID")
    void acceptsMinimumValidAdvisorId() {

        Advisor advisor = new Advisor(
                1,
                "Aviral Singh",
                "aviral@example.com",
                "hashedPassword123",
                LocalDate.of(2026, 9, 2)
        );

        assertEquals(1, advisor.getAdvisorId());
    }

    @Test
    @DisplayName("rejects an advisor ID less than 1")
    void rejectsInvalidAdvisorId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Advisor(
                        0,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        LocalDate.of(2026, 9, 2)
                )
        );
    }

    @Test
    @DisplayName("rejects a negative advisor ID")
    void rejectsNegativeAdvisorId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Advisor(
                        -1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        LocalDate.of(2026, 9, 2)
                )
        );
    }

    @Test
    @DisplayName("rejects a null advisor name")
    void rejectsNullAdvisorName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Advisor(
                        1,
                        null,
                        "aviral@example.com",
                        "hashedPassword123",
                        LocalDate.of(2026, 9, 2)
                )
        );
    }

    @Test
    @DisplayName("rejects a blank advisor name")
    void rejectsBlankAdvisorName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Advisor(
                        1,
                        "   ",
                        "aviral@example.com",
                        "hashedPassword123",
                        LocalDate.of(2026, 9, 2)
                )
        );
    }

    @Test
    @DisplayName("rejects a null email")
    void rejectsNullEmail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Advisor(
                        1,
                        "Aviral Singh",
                        null,
                        "hashedPassword123",
                        LocalDate.of(2026, 9, 2)
                )
        );
    }

    @Test
    @DisplayName("rejects a blank email")
    void rejectsBlankEmail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Advisor(
                        1,
                        "Aviral Singh",
                        "   ",
                        "hashedPassword123",
                        LocalDate.of(2026, 9, 2)
                )
        );
    }

    @Test
    @DisplayName("rejects a null password hash")
    void rejectsNullPasswordHash() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Advisor(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        null,
                        LocalDate.of(2026, 9, 2)
                )
        );
    }

    @Test
    @DisplayName("rejects a blank password hash")
    void rejectsBlankPasswordHash() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Advisor(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "   ",
                        LocalDate.of(2026, 9, 2)
                )
        );
    }

    @Test
    @DisplayName("rejects a null hire date")
    void rejectsNullHireDate() {

        assertThrows(
                NullPointerException.class,
                () -> new Advisor(
                        1,
                        "Aviral Singh",
                        "aviral@example.com",
                        "hashedPassword123",
                        null
                )
        );
    }

    @Test
    @DisplayName("preserves the supplied hire date")
    void preservesHireDate() {

        LocalDate hireDate = LocalDate.of(2025, 1, 15);

        Advisor advisor = new Advisor(
                1,
                "Aviral Singh",
                "aviral@example.com",
                "hashedPassword123",
                hireDate
        );

        assertEquals(hireDate, advisor.getHireDate());
    }

    @Test
    @DisplayName("preserves the supplied password hash")
    void preservesPasswordHash() {

        String passwordHash = "hashedPassword123";

        Advisor advisor = new Advisor(
                1,
                "Aviral Singh",
                "aviral@example.com",
                passwordHash,
                LocalDate.of(2026, 9, 2)
        );

        assertEquals(passwordHash, advisor.getPasswordHash());
    }
}

