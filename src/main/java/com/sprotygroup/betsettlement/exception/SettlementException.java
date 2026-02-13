package com.sprotygroup.betsettlement.exception;

import lombok.Getter;

@Getter
public class SettlementException extends RuntimeException{
    private final String causeMsg;
    public SettlementException(String message, String causeMsg) {
        super(message);
        this.causeMsg = causeMsg;
    }
}
