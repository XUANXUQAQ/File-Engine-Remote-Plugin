package file.engine.remote.utils;

import com.google.gson.Gson;
import file.engine.remote.utils.gson.GsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class VersionUtil {

    public final static double CURRENT_VERSION = 1.1;

    private static String updateURL;

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getVersionInfo() throws IOException {
        StringBuilder jsonUpdate = new StringBuilder();
        URL updateServer = new URL("https://cdn.jsdelivr.net/gh/XUANXUQAQ/File-Engine-Version/Plugins%20Repository/RemotePluginVersion.json");
        URLConnection uc = updateServer.openConnection();
        uc.setConnectTimeout(3 * 1000);
        //防止屏蔽程序抓取而返回403错误
        uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.81 Safari/537.36 Edg/104.0.1293.54");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8))) {
            String eachLine;
            while ((eachLine = br.readLine()) != null) {
                jsonUpdate.append(eachLine);
            }
        }
        Gson gson = GsonUtil.getInstance().getGson();
        return gson.fromJson(jsonUpdate.toString(), Map.class);
    }

    public static String _getUpdateURL() {
        return updateURL;
    }

    public static boolean _isLatest() throws IOException {
        Map<String, Object> json = getVersionInfo();
        String latestVersion = (String) json.get("version");
        if (Double.parseDouble(latestVersion) >  CURRENT_VERSION) {
            updateURL = (String) json.get("url");
            return false;
        } else {
            return true;
        }
    }

    public static String _getPluginVersion() {
        return String.valueOf(CURRENT_VERSION);
    }
}
