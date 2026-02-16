package com.sprotygroup.betsettlement.event;

public record EventOutcome(
        Long eventId,
        String eventName,
        Long eventWinnerId) {
}
