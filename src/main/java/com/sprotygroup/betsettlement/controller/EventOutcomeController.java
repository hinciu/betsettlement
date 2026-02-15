package com.sprotygroup.betsettlement.controller;

import com.sprotygroup.betsettlement.dto.EventOutcomeRequest;
import com.sprotygroup.betsettlement.mapper.EventOutcomeMapper;
import com.sprotygroup.betsettlement.event.EventOutcome;
import com.sprotygroup.betsettlement.producer.EventOutcomeProducer;
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
public class EventOutcomeController {

    public static final String BASE_URL = "/api/event-outcomes";

    private final EventOutcomeProducer publisher;
    private final EventOutcomeMapper mapper;


    @PostMapping
    public ResponseEntity<EventOutcome> publish(@Valid @RequestBody EventOutcomeRequest request) {
        EventOutcome outcome = mapper.toEventOutcome(request);
        publisher.publish(outcome);
        return ResponseEntity.accepted().body(outcome);
    }
}
