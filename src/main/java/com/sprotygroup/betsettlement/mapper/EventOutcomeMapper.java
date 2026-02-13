package com.sprotygroup.betsettlement.mapper;

import com.sprotygroup.betsettlement.dto.EventOutcomeRequest;
import com.sprotygroup.betsettlement.event.EventOutcome;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventOutcomeMapper {
    EventOutcome toEventOutcome(EventOutcomeRequest request);
}
