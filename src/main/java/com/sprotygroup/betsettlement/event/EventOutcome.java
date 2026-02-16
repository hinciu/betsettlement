package com.sprotygroup.betsettlement.event;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sport event outcome published to Kafka")
public record EventOutcome(
        @Schema(description = "Unique identifier of the sport event", example = "5001")
        Long eventId,

        @Schema(description = "Name of the sport event", example = "Championship Final")
        String eventName,

        @Schema(description = "Unique identifier of the winning team/player", example = "9001")
        Long eventWinnerId) {
}
