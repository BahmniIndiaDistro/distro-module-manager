package org.bahmni.indiadistro.config;

import java.util.Map;

public class ApplicationProperties {
    private String indiaDistroModulesDir;
    private String bahmniConfigDir;

    public ApplicationProperties() {
        Map<String, String> env = System.getenv();
        this.indiaDistroModulesDir = env.get("INDIA_DISTRO_MODULES_DIR");
        this.bahmniConfigDir = env.get("BAHMNI_CONFIG_DIR");
    }

    public String getIndiaDistroModulesDir() {
        return indiaDistroModulesDir;
    }

    public String getBahmniConfigDir() {
        return bahmniConfigDir;
    }
}
