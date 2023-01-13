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
    public static final String TMP_PATH = Path.of(CONFIGURATION_PATH, "tmp").toString();
    private static final int DEFAULT_PORT = 23333;
    private Map<String, Object> configMap;
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
        return (int) configMap.getOrDefault("port", DEFAULT_PORT);
    }

    @SuppressWarnings("unchecked")
    private void readConfiguration(String configPath) throws IOException {
        GsonUtil gsonUtil = GsonUtil.getInstance();
        Gson gson = gsonUtil.getGson();
        configMap = gson.fromJson(Files.readString(Path.of(configPath)), Map.class);
    }

    private ConfigsUtil(String path) throws IOException {
        File configPath = new File(CONFIGURATION_PATH);
        if (!configPath.exists()) {
            boolean mkdirs = configPath.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("mkdir failed..." + CONFIGURATION_PATH);
            }
        }
        File tmpFile = new File(TMP_PATH);
        if (!tmpFile.exists()) {
            boolean mkdirs = tmpFile.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("mkdir tmp failed..." + TMP_PATH);
            }
        }
        deleteDir(tmpFile);
        File configFile = new File(CONFIGURATION_FILE);
        if (!configFile.exists()) {
            boolean newFile = configFile.createNewFile();
            if (!newFile) {
                throw new RuntimeException("create settings.json failed..." + CONFIGURATION_FILE);
            }
            //初始化默认配置
            configMap = new HashMap<>();
            configMap.put("port", DEFAULT_PORT);
            Gson gson = GsonUtil.getInstance().getGson();
            String json = gson.toJson(configMap);
            Files.writeString(Path.of(CONFIGURATION_FILE), json);
        } else {
            //读取配置
            readConfiguration(path);
        }
    }

    /**
     * 清空一个目录，不删除目录本身
     *
     * @param file 目录文件
     */
    private static void deleteDir(File file) {
        if (!file.exists()) {
            return;
        }
        File[] content = file.listFiles();//取得当前目录下所有文件和文件夹
        if (content == null) {
            return;
        }
        for (File temp : content) {
            //直接删除文件
            if (temp.isDirectory()) {//判断是否是目录
                deleteDir(temp);//递归调用，删除目录里的内容
            }
            //删除空目录
            if (!temp.delete()) {
                System.err.println("Failed to delete " + temp.getAbsolutePath());
            }
        }
    }
}
