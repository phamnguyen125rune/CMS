package IVS.CMS;

import java.io.File;
import java.nio.file.Files;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CmsApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(CmsApplication.class, args);
    }

    private static void loadDotEnv() {
        File envFile = new File(".env");
        if (envFile.exists()) {
            try {
                Files.readAllLines(envFile.toPath()).forEach(line -> {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        int index = trimmed.indexOf("=");
                        String key = trimmed.substring(0, index).trim();
                        String value = trimmed.substring(index + 1).trim();
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                });
            } catch (Exception ignored) {
            }
        }
    }
}
