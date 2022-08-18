package file.engine.remote.utils.configs;

import com.google.gson.Gson;
import file.engine.remote.utils.gson.GsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigsUtil {
    public static final String CONFIGURATION_PATH = "plugins\\Plugin configuration files\\Remote";
    public static final String CONFIGURATION_FILE = Path.of(CONFIGURATION_PATH, "settings.json").toString();
    private static final int DEFAULT_PORT = 23333;
    private static final int DEFAULT_MAX_RESULTS_PER_PAGE = 20;
    private Map<String, Object> configEntity;
    private static volatile ConfigsUtil instance;

    public static ConfigsUtil getInstance() throws IOException {
        if (instance == null) {
            synchronized (ConfigsUtil.class) {
                if (instance == null) {
                    instance = new ConfigsUtil(CONFIGURATION_FILE);
                }
            }
        }
        return instance;
    }

    public int getPort() {
        return (int) configEntity.getOrDefault("port", DEFAULT_PORT);
    }

    public int getMaxResultsPerPage() {
        return (int) configEntity.getOrDefault("maxResultsPerPage", DEFAULT_MAX_RESULTS_PER_PAGE);
    }

    private void readConfiguration(String configPath) throws IOException {
        GsonUtil gsonUtil = GsonUtil.getInstance();
        Gson gson = gsonUtil.getGson();
        configEntity = gson.fromJson(Files.readString(Path.of(configPath)), Map.class);
    }

    private ConfigsUtil(String path) throws IOException {
        File configPath = new File(CONFIGURATION_PATH);
        if (!configPath.exists()) {
            boolean mkdirs = configPath.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("mkdir failed..." + CONFIGURATION_PATH);
            }
        }
        File configFile = new File(CONFIGURATION_FILE);
        if (!configFile.exists()) {
            boolean newFile = configFile.createNewFile();
            if (!newFile) {
                throw new RuntimeException("create settings.json failed..." + CONFIGURATION_FILE);
            }
            //初始化默认配置
            configEntity = new HashMap<>() {{
                put("port", DEFAULT_PORT);
                put("maxResultsPerPage", DEFAULT_MAX_RESULTS_PER_PAGE);
            }};
            Gson gson = GsonUtil.getInstance().getGson();
            String json = gson.toJson(configEntity);
            Files.writeString(Path.of(CONFIGURATION_FILE), json);
        } else {
            //读取配置
            readConfiguration(path);
        }
    }
}
