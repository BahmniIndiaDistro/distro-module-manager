package org.bahmni.indiadistro.util;

public class StringUtil {

    public static String ensureSuffix(String value, String suffix) {
        String trimmedValue = value.trim();
        if (trimmedValue.endsWith(suffix)) {
            return trimmedValue;
        } else {
            return trimmedValue + suffix;
        }
    }

    public static String removeSuffix(String value, String suffix) {
        String trimmedValue = value.trim();
        if (trimmedValue.endsWith(suffix)) {
            return trimmedValue.substring(0, trimmedValue.lastIndexOf(suffix));
        } else {
            return trimmedValue;
        }
    }

    public static String removePrefix(String value, String prefix) {
        String trimmedValue = value.trim();
        if (trimmedValue.startsWith(prefix)) {
            return trimmedValue.substring(prefix.length());
        } else {
            return trimmedValue;
        }
    }
}
