package org.bahmni.indiadistro.installer;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class AnalyticsInstaller {
    private static final String analyticsPluginsFolderPath = "/var/lib/bahmni-shiny/plugins";
    private static final String analyticsPreferencesFolderPath = "/var/lib/bahmni-shiny/preferences";

    public void installForModule(String moduleName) throws IOException {
        String urlFormatForDaoFile = "https://raw.githubusercontent.com/BahmniIndiaDistro/distro/master/%s/analytics/dao.R";
        String urlFormatForConfigFile = "https://raw.githubusercontent.com/BahmniIndiaDistro/distro/master/%s/analytics/config.json";
        File modulePluginFolder = new File(analyticsPluginsFolderPath, moduleName);

        URL daoFileURL = new URL(String.format(urlFormatForDaoFile, moduleName));
        File daoFile = new File(modulePluginFolder, "dao.R");
        FileUtils.copyURLToFile(daoFileURL, daoFile);

        URL configFileURL = new URL(String.format(urlFormatForConfigFile, moduleName));
        File configFile = new File(modulePluginFolder, "config.json");
        FileUtils.copyURLToFile(configFileURL, configFile);
    }
}
