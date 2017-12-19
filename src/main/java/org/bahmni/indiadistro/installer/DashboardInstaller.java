package org.bahmni.indiadistro.installer;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.indiadistro.ModuleManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.bahmni.indiadistro.ModuleManager.BAHMNI_CONFIG_DIR;
import static org.bahmni.indiadistro.ModuleManager.MODULES_DIRECTORY;

public class DashboardInstaller {
    private static final String DASHBOARD_FILE_PATH = "openmrs/apps/clinical/dashboard.json";

    public void installForModule(String moduleName) {
        File pathToModule = new File(MODULES_DIRECTORY, moduleName);
        File sourceDashboardFile = new File(pathToModule, DASHBOARD_FILE_PATH);
        File destinationDashboardFile = new File(BAHMNI_CONFIG_DIR, DASHBOARD_FILE_PATH);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        try {
            Map<String, Object> moduleDashboards = objectMapper.readValue(sourceDashboardFile,
                    new TypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> bahmniDashboards = objectMapper.readValue(destinationDashboardFile,
                    new TypeReference<Map<String, Object>>() {
                    });

            String moduleKey = StringUtils.lowerCase(moduleName);
            bahmniDashboards.put(moduleKey, moduleDashboards.get(moduleKey));

            objectMapper.writeValue(destinationDashboardFile, bahmniDashboards);
        } catch (IOException e) {
            throw new RuntimeException("Dashboard Installation failed", e);
        }

    }
}