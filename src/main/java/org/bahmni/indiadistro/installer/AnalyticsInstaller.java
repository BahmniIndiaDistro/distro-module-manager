package org.bahmni.indiadistro.installer;

import org.apache.commons.io.FileUtils;
import org.bahmni.indiadistro.config.ApplicationProperties;

import java.io.File;
import java.io.IOException;

public class AnalyticsInstaller {
    private static final String ANALYTICS_PLUGINS_FOLDER_PATH = "/var/lib/bahmni-shiny/plugins";
    private static final String ANALYTICS_PREFERENCES_FOLDER_PATH = "/var/lib/bahmni-shiny/preferences";
    private static final String DAO_FILE_PATH = "analytics/dao.R";
    private static final String CONFIG_FILE_PATH = "analytics/config.json";
    private static final String DASHBOARD_FILE_PATH = "analytics/dashboard.json";
    private static final String COL_DEF_FILE_PATH = "analytics/columns.json";
    private ApplicationProperties applicationProperties;

    public AnalyticsInstaller(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void installForModule(String moduleName) throws IOException {
        System.out.println(String.format("Installing Analytics for %s", moduleName));
        File moduleDirectory = new File(applicationProperties.getIndiaDistroModulesDir(), moduleName);


        File modulePluginFolder = new File(ANALYTICS_PLUGINS_FOLDER_PATH, moduleName);

        File sourceDaoFile = new File(moduleDirectory, DAO_FILE_PATH);
        File targetDaoFile = new File(modulePluginFolder, "dao.R");
        FileUtils.copyFile(sourceDaoFile, targetDaoFile);

        File sourceConfigFile = new File(moduleDirectory, CONFIG_FILE_PATH);
        File targetConfigFile = new File(modulePluginFolder, "config.json");
        FileUtils.copyFile(sourceConfigFile, targetConfigFile);

        File sourceDashboardFile = new File(moduleDirectory, DASHBOARD_FILE_PATH);
        String dashboardFileName = String.format("%s-dashboard.json", moduleName);
        File targetDashboardFile = new File(ANALYTICS_PREFERENCES_FOLDER_PATH, dashboardFileName);
        if (sourceDashboardFile.exists()) {
            FileUtils.copyFile(sourceDashboardFile, targetDashboardFile);
        }

        File sourceColumnDefFile = new File(moduleDirectory, COL_DEF_FILE_PATH);
        String colDefFileName = String.format("%s-columns.json", moduleName);
        File targetColDefFile = new File(ANALYTICS_PREFERENCES_FOLDER_PATH, colDefFileName);
        if (sourceColumnDefFile.exists()) {
            FileUtils.copyFile(sourceColumnDefFile, targetColDefFile);
        }
    }
}
