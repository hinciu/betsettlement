package com.sprotygroup.betsettlement.mapper;

import com.sprotygroup.betsettlement.event.BetSettlement;
import com.sprotygroup.betsettlement.model.Bet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BetSettlementMapper {

    @Mapping(target = "betId", source = "bet.id")
    @Mapping(target = "userId", source = "bet.userId")
    @Mapping(target = "eventId", source = "bet.eventId")
    @Mapping(target = "eventMarketId", source = "bet.eventMarketId")
    @Mapping(target = "eventWinnerId", source = "bet.eventWinnerId")
    @Mapping(target = "betAmount", source = "bet.betAmount")
    BetSettlement toBetSettlement(Bet bet);
}
