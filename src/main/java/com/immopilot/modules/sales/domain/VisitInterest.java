package com.immopilot.modules.sales.domain;

public enum VisitInterest {
    LOW("Faible"),
    MEDIUM("Moyen"),
    HIGH("Fort");

    private final String label;

    VisitInterest(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
