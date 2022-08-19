package file.engine.remote.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class OpenUtil {
    public static void openFolderByExplorer(String dir) throws IOException {
        Runtime.getRuntime().exec("explorer.exe /select, \"" + dir + "\"");
    }

    public static void openBrowser(String url) throws IOException {
        Desktop desktop;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            desktop.browse(URI.create(url));
        }
    }
}
