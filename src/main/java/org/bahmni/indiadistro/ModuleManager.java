package org.bahmni.indiadistro;


import org.apache.commons.lang3.StringUtils;
import org.bahmni.indiadistro.installer.AnalyticsInstaller;
import org.bahmni.indiadistro.installer.ReportsInstaller;

import java.io.IOException;

public class ModuleManager {
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

        new ReportsInstaller().installForModule(moduleName);
//        new AnalyticsInstaller().installForModule(moduleName);
    }
}
