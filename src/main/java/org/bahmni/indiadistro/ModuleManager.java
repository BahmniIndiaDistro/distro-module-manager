package org.bahmni.indiadistro;


import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.indiadistro.config.ApplicationProperties;
import org.bahmni.indiadistro.installer.*;

import java.io.IOException;


public class ModuleManager {
    private static final Logger logger = LogManager.getLogger(ModuleManager.class);

    public static void main(String[] args) throws IOException {
        String argumentMissingMessage = "USAGE:- java -jar ModuleManager.jar install <modulename>";
        if (args.length < 2) {
            System.out.println(argumentMissingMessage);
            throw new RuntimeException("Missing argument(s).");
        }
        if (!"Install".equalsIgnoreCase(args[0])) {
            System.out.println(argumentMissingMessage);
            throw new RuntimeException("Illegal argument(s).");
        }
        String moduleName = StringUtils.trim(args[1]);
        String message = String.format("Installing module %s", moduleName);
        System.out.println(message);
        logger.info(message);

        ApplicationProperties applicationProperties = new ApplicationProperties(System.getenv());
        ConceptUUIDManager conceptUUIDManager = new ConceptUUIDManager(applicationProperties);

        new ReferenceDataManager(applicationProperties).uploadForModule(moduleName);
        new FormInstaller(conceptUUIDManager, applicationProperties).installForModule(moduleName);
        new DashboardInstaller(applicationProperties).installForModule(moduleName);
        new ReportsInstaller(applicationProperties).installForModule(moduleName);
        new AnalyticsInstaller(applicationProperties).installForModule(moduleName);
    }

}
