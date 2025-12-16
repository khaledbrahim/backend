package com.immopilot.modules.sales.domain;

public enum SaleStatus {
    DRAFT("Brouillon"),
    ON_MARKET("En Vente"),
    VISITS("Visites en cours"),
    OFFERS("Offres reçues"),
    NEGOTIATION("Négociation"),
    OFFER_ACCEPTED("Offre acceptée"),
    COMPROMIS_SIGNED("Compromis signé"),
    PROMESSE_SIGNED("Promesse signée"),
    ACT_SIGNED("Acte signé"),
    ABANDONED("Abandonné"),
    CANCELLED("Annulé");

    private final String label;

    SaleStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
