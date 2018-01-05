package org.bahmni.indiadistro.installer;


import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.indiadistro.config.ApplicationProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ReportsInstaller {
    private static final String BAHMNI_REPORTS_SQL_PATH = "openmrs/apps/reports/sql";
    private static final String OPENMRS_REPORTS_CONFIG_FILE_NAME = "openmrs/apps/reports/reports.json";
    private ApplicationProperties applicationProperties;

    private static final Logger logger = LogManager.getLogger(ReportsInstaller.class);

    public ReportsInstaller(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void installForModule(String moduleName) {
        String message = String.format("Installing Reports for %s", moduleName);
        System.out.println(message);
        logger.info(message);

        File moduleDirectory = new File(applicationProperties.getIndiaDistroModulesDir(), moduleName);
        addConfig(moduleDirectory);
        addSQLFiles(moduleDirectory);
    }

    private void addSQLFiles(File moduleDirectory) {
        File source = new File(moduleDirectory, BAHMNI_REPORTS_SQL_PATH);
        File dest = new File(applicationProperties.getBahmniConfigDir(), BAHMNI_REPORTS_SQL_PATH);
        if (!source.exists()) {
            return;
        }
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            throw new RuntimeException("Reports Installation failed", e);
        }
    }

    private void addConfig(File moduleBasePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File reportsFile = new File(applicationProperties.getBahmniConfigDir(), OPENMRS_REPORTS_CONFIG_FILE_NAME);

            Map<String, Object> moduleReports = objectMapper.readValue(new File(moduleBasePath, OPENMRS_REPORTS_CONFIG_FILE_NAME),
                    new TypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> baseReports = objectMapper.readValue(reportsFile,
                    new TypeReference<Map<String, Object>>() {
                    });

            for (String key : moduleReports.keySet()) {
                baseReports.put(key, moduleReports.get(key));
            }

            objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
            objectMapper.writeValue(reportsFile, baseReports);
        } catch (IOException e) {
            throw new RuntimeException("Reports Installation failed", e);
        }
    }
}
