package com.sprotygroup.betsettlement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventOutcomeRequest {
    @NotNull
    private Long eventId;
    @NotBlank
    private String eventName;
    @NotNull
    private Long eventWinnerId;
}
