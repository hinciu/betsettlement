package com.sprotygroup.betsettlement.controller;

import com.sprotygroup.betsettlement.dto.EventOutcomeRequest;
import com.sprotygroup.betsettlement.mapper.EventOutcomeMapper;
import com.sprotygroup.betsettlement.event.EventOutcome;
import com.sprotygroup.betsettlement.producer.EventOutcomeProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sprotygroup.betsettlement.controller.EventOutcomeController.BASE_URL;

@RestController
@RequestMapping(BASE_URL)
@AllArgsConstructor
@Tag(name = "Event Outcomes", description = "API for publishing sport event outcomes to trigger bet settlement")
public class EventOutcomeController {

    public static final String BASE_URL = "/api/event-outcomes";

    private final EventOutcomeProducer publisher;
    private final EventOutcomeMapper mapper;

    @Operation(
            summary = "Publish Event Outcome",
            description = "Publishes a sport event outcome to Kafka. The outcome will be consumed by the settlement service to settle winning bets."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Event outcome accepted and published to Kafka",
                    content = @Content(schema = @Schema(implementation = EventOutcome.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body - validation failed"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - failed to publish to Kafka"
            )
    })
    @PostMapping
    public ResponseEntity<EventOutcome> publish(@Valid @RequestBody EventOutcomeRequest request) {
        EventOutcome outcome = mapper.toEventOutcome(request);
        publisher.publish(outcome);
        return ResponseEntity.accepted().body(outcome);
    }
}
