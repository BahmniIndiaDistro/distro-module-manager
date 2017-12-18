package org.bahmni.indiadistro.installer;


import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ReportsInstaller {

    private static final String MODULES_DIRECTORY = System.getenv("INDIA_DISTRO_MODULES_DIR");
    private static final String BAHMNI_CONFIG_DIR = System.getenv("BAHMNI_CONFIG_DIR");
    private static final String BAHMNI_REPORTS_SQL_PATH = "openmrs/apps/reports/sql";
    private static final String OPENMRS_REPORTS_CONFIG_FILE_NAME = "openmrs/apps/reports/reports.json";

    public static void main(String[] args) {
        new ReportsInstaller().installForModule("hypertension");
    }

    public void installForModule(String moduleName) {
        File moduleDirectory = new File(MODULES_DIRECTORY, moduleName);
        addConfig(moduleDirectory);
        addSQLFiles(moduleDirectory);
    }

    private void addSQLFiles(File moduleDirectory) {
        File source = new File(moduleDirectory, BAHMNI_REPORTS_SQL_PATH);
        File dest = new File(BAHMNI_CONFIG_DIR, BAHMNI_REPORTS_SQL_PATH);
        if (!source.exists()) {
            return;
        }
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addConfig(File moduleBasePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> moduleReports = objectMapper.readValue(new File(moduleBasePath, OPENMRS_REPORTS_CONFIG_FILE_NAME),
                    new TypeReference<Map<String, Object>>() {});
            Map<String, Object> baseReports = objectMapper.readValue(new File(BAHMNI_CONFIG_DIR, OPENMRS_REPORTS_CONFIG_FILE_NAME),
                    new TypeReference<Map<String, Object>>() {});

            for (String key : moduleReports.keySet()) {
                baseReports.put(key, moduleReports.get(key));
            }

            objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
            objectMapper.writeValue(new File(BAHMNI_CONFIG_DIR, OPENMRS_REPORTS_CONFIG_FILE_NAME), baseReports);
        } catch (IOException e) {
            throw new RuntimeException("Reports Installation failed", e);
        }
    }
}
