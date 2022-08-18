package file.engine.remote.utils;

import java.io.IOException;

public class OpenFilePathUtil {
    public static void openFolderByExplorer(String dir) throws IOException {
        Runtime.getRuntime().exec("explorer.exe /select, \"" + dir + "\"");
    }
}
