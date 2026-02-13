package com.sprotygroup.betsettlement.service;

import com.sprotygroup.betsettlement.event.EventOutcome;
import com.sprotygroup.betsettlement.model.EventOutcomeStatus;
import com.sprotygroup.betsettlement.model.FailedEventOutcome;
import com.sprotygroup.betsettlement.repository.FailedEventOutcomeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailedEventOutcomeService {

    private final FailedEventOutcomeRepository failedEventOutcomeRepository;

    @Transactional
    public void saveFailedEvent(EventOutcome outcome, String errorMessage) {
        FailedEventOutcome failedEvent = FailedEventOutcome.builder()
                .eventId(outcome.eventId())
                .eventName(outcome.eventName())
                .eventWinnerId(outcome.eventWinnerId())
                .errorMessage(errorMessage)
                .failedAt(LocalDateTime.now())
                .status(EventOutcomeStatus.FAILED)
                .build();

        failedEventOutcomeRepository.save(failedEvent);
        log.warn("Saved failed event outcome to database: eventId={}", outcome.eventId());
    }
}
