package org.bahmni.indiadistro;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestUtil {
    public static Map<String, String> loadTestProperties() {
        Map<String, String> env = new HashMap<>();

        try {
            InputStream inputStream = TestUtil.class.getResourceAsStream("/test.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            for (Object key : properties.keySet()) {
                env.put(key.toString(), properties.getProperty(key.toString()));
            }
        } catch (Exception ignored) {
            System.out.print("Error while setting up test properties!");
        }
        return env;
    }
}
