package com.sprotygroup.betsettlement.dto;

public record SettlementTask(
        Long eventId,
        Long winnerId,
        int bucketId,
        int totalBuckets
) {}

