package org.bahmni.indiadistro;


import org.bahmni.indiadistro.installer.AnalyticsInstaller;

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
        System.out.println(String.format("Installing module %s", args[1]));
        new AnalyticsInstaller().installForModule(args[1]);
    }
}
