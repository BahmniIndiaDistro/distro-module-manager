package org.bahmni.indiadistro.config;

import java.util.Map;

public class ApplicationProperties {
    private String bahmniConfigDir;
    private String indiaDistroModulesDir;
    private String openmrsBaseURL;
    private String openmrsAPIUserName;
    private String openmrsAPIUserPassword;
    private String waitIntervalForCSVUpload;

    public ApplicationProperties(Map<String, String> env) {
        this.indiaDistroModulesDir = env.get("INDIA_DISTRO_MODULES_DIR");
        this.bahmniConfigDir = env.get("BAHMNI_CONFIG_DIR");
        this.openmrsBaseURL = env.get("OPENMRS_BASE_URL");
        this.openmrsAPIUserName = env.get("OPENMRS_API_USERNAME");
        this.openmrsAPIUserPassword = env.get("OPENMRS_API_PASSWORD");
        this.waitIntervalForCSVUpload = env.get("WAIT_INTERVAL_FOR_CSV_UPLOAD");
    }

    public String getIndiaDistroModulesDir() {
        return indiaDistroModulesDir;
    }

    public String getBahmniConfigDir() {
        return bahmniConfigDir;
    }

    public String getOpenmrsBaseURL() {
        return openmrsBaseURL;
    }

    public String getOpenmrsAPIUserName() {
        return openmrsAPIUserName;
    }

    public String getOpenmrsAPIUserPassword() {
        return openmrsAPIUserPassword;
    }

    public int getWaitIntervalForCSVUpload() {
        return Integer.parseInt(waitIntervalForCSVUpload);
    }
}
