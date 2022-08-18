package file.engine.remote.httpd;

import com.google.gson.Gson;
import file.engine.remote.utils.gson.GsonUtil;
import lombok.Data;

@Data
public class ResBody {

    private final int code;

    private final String message;

    private final Object data;

    private ResBody(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ResBody success(Object data) {
        return new ResBody(20000, "success", data);
    }

    public static ResBody error(String msg) {
        return new ResBody(40000, "error: " + msg, null);
    }

    @Override
    public String toString() {
        GsonUtil gsonUtil = GsonUtil.getInstance();
        Gson gson = gsonUtil.getGson();
        return gson.toJson(this);
    }
}
