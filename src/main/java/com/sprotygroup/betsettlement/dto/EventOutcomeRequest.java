package com.sprotygroup.betsettlement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to publish a sport event outcome")
public class EventOutcomeRequest {

    @NotNull
    @Schema(description = "Unique identifier of the sport event", example = "5001", required = true)
    private Long eventId;

    @NotBlank
    @Schema(description = "Name of the sport event", example = "Championship Final", required = true)
    private String eventName;

    @NotNull
    @Schema(description = "Unique identifier of the winning team/player", example = "9001", required = true)
    private Long eventWinnerId;
}
