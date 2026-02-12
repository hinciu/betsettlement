package com.sprotygroup.betsettlement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "failed_event_outcomes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedEventOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private Long eventWinnerId;

    @Column(length = 2000)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime failedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EventOutcomeStatus status;

    private LocalDateTime processedAt;
}
