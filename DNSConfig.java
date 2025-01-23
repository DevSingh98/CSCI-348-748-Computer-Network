import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class DNSConfig {
    private static final String CONFIG_FILE = "DNSConfig.txt";
    private static Map<String, String> configMap = new HashMap<>();

    static {
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    configMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading config file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return configMap.getOrDefault(key, null);
    }
}
