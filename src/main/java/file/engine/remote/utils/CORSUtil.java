package file.engine.remote.utils;

public class CORSUtil {

    private static final String cors = "File_Engine_Remote_CORS";

    public static boolean isCorsEnabled() {
        return "true".equals(System.getProperty(cors));
    }
}
