package org.bahmni.indiadistro;


import org.apache.commons.lang3.StringUtils;
import org.bahmni.indiadistro.installer.AnalyticsInstaller;
import org.bahmni.indiadistro.installer.DashboardInstaller;
import org.bahmni.indiadistro.installer.ReportsInstaller;

import java.io.IOException;

public class ModuleManager {
    public static final String MODULES_DIRECTORY = System.getenv("INDIA_DISTRO_MODULES_DIR");
    public static final String BAHMNI_CONFIG_DIR = System.getenv("BAHMNI_CONFIG_DIR");

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("USAGE:- java -jar ModuleManager.jar install <modulename>");
            throw new RuntimeException("Missing argument(s).");
        }
        if (!"Install".equalsIgnoreCase(args[0])) {
            System.out.println("USAGE:- java -jar ModuleManager.jar install <modulename>");
            throw new RuntimeException("Illegal argument(s).");
        }
        String moduleName = StringUtils.trim(args[1]);
        System.out.println(String.format("Installing module %s", moduleName));

        new DashboardInstaller().installForModule(moduleName);
        new ReportsInstaller().installForModule(moduleName);
//        new AnalyticsInstaller().installForModule(moduleName);
    }
}
