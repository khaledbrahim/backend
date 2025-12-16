package com.immopilot.modules.sales.domain;

public enum OfferStatus {
    PENDING("En attente"),
    ACCEPTED("Acceptée"),
    REJECTED("Refusée"),
    COUNTER_OFFER("Contre-proposition");

    private final String label;

    OfferStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
