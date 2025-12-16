package com.immopilot;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithTest {

    @Test
    @org.junit.jupiter.api.Disabled("Architecture rules pending configuration")
    void verifyModulith() {
        var modules = ApplicationModules.of(ImmoPilotApplication.class);
        modules.verify();

        // Optional: Generate documentation (C4 etc)
        new Documenter(modules).writeDocumentation();
    }
}
