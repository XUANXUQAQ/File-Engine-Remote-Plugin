package file.engine.remote.utils;

public class CORSUtil {

    private static final String cors = "File_Engine_Remote_CORS";

    static {
        // TODO 调试设置为true
        System.setProperty(cors, "false");
    }

    public static boolean isCorsEnabled() {
        return Boolean.parseBoolean(System.getProperty(cors));
    }
}
