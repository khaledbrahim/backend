package com.immopilot.modules.users.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuItem {
    private String label;
    private String route;
    private String icon;
    private boolean isEnabled;
    // For frontend use, we compute isEnabled on backend based on roles
}
