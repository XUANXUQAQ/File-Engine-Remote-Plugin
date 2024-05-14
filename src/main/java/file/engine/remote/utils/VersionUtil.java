package file.engine.remote.utils;

import com.google.gson.Gson;
import file.engine.remote.Plugin;
import file.engine.remote.utils.gson.GsonUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class VersionUtil {

    public final static double CURRENT_VERSION = 1.9;

    private static String updateURL;

    private static final ConcurrentLinkedQueue<String> downloadTasks = new ConcurrentLinkedQueue<>();


    public static void registerDownloadListener() throws ClassNotFoundException {
        Class<?> downloadManagerClass = Class.forName("file.engine.services.download.DownloadManager");
        Plugin.registerFileEngineEventListener("file.engine.event.handler.impl.download.DownloadDoneEvent",
                "downloadDoneListener",
                (c, o) -> {
                    try {
                        Field downloadManagerField = c.getField("downloadManager");
                        Object downloadManager = downloadManagerField.get(o);
                        Field savePathField = downloadManagerClass.getField("savePath");
                        Field fileNameField = downloadManagerClass.getField("fileName");
                        Field urlField = downloadManagerClass.getField("url");
                        String savePath = (String) savePathField.get(downloadManager);
                        String fileName = (String) fileNameField.get(downloadManager);
                        String url = (String) urlField.get(downloadManager);
                        downloadTasks.add(url + new File(savePath, fileName).getAbsolutePath());
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getVersionInfo() throws IOException {
        String url = "https://cdn.jsdelivr.net/gh/XUANXUQAQ/File-Engine-Version/Plugins%20Repository/RemotePluginVersion.json";
        String savePath = "tmp";
        String fileName = "RemotePluginVersion.json";
        try {
            Plugin.checkEvent("file.engine.event.handler.impl.download.StartDownloadEvent2", Collections.EMPTY_MAP);
            Plugin.sendEventToFileEngine("file.engine.event.handler.impl.download.StartDownloadEvent2",
                    url,
                    fileName,
                    savePath);
            long start = System.currentTimeMillis();
            while (!downloadTasks.contains(url + new File(savePath, fileName).getAbsolutePath())) {
                if (System.currentTimeMillis() - start > 10000) {
                    throw new RuntimeException("获取插件Remote更新信息失败");
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
            StringBuilder jsonUpdate = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(savePath, fileName)), StandardCharsets.UTF_8))) {
                String eachLine;
                while ((eachLine = br.readLine()) != null) {
                    jsonUpdate.append(eachLine);
                }
            }
            return GsonUtil.INSTANCE.getGson().fromJson(jsonUpdate.toString(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            StringBuilder jsonUpdate = new StringBuilder();
            URL updateServer = new URL(url);
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
    }

    public static String _getUpdateURL() {
        return updateURL;
    }

    public static boolean _isLatest() throws IOException {
        Map<String, Object> json = getVersionInfo();
        String latestVersion = (String) json.get("version");
        if (Double.parseDouble(latestVersion) > CURRENT_VERSION) {
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
