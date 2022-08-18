package file.engine.remote.httpd;

import fi.iki.elonen.NanoHTTPD;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class HttpServer extends NanoHTTPD {
    private String searchText;
    private String[] searchCase;
    private String[] keywords;
    private volatile boolean isSearchInfoSet = false;
    private volatile ConcurrentLinkedQueue<String> searchResults;
    private static final Pattern semicolon = Pattern.compile(";");

    public HttpServer(int port) throws IOException {
        super(port);
        start(SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        if (Method.POST.equals(method)) {
            Map<String, List<String>> parameters = session.getParameters();
            List<String> inputTextList = parameters.get("inputText");
            if (!inputTextList.isEmpty()) {
                String inputText = inputTextList.get(0);
                if (inputText.length() < 300) {
                    setSearchInfo(inputText);
                    searchResults = null;
                    isSearchInfoSet = true;
                    final long startWaitingTime = System.currentTimeMillis();
                    while (searchResults == null && System.currentTimeMillis() - startWaitingTime > 3000)
                        Thread.onSpinWait();
                    if (searchResults != null) {
                        return NanoHTTPD.newFixedLengthResponse(ResBody.success(searchResults).toString());
                    } else {
                        return NanoHTTPD.newFixedLengthResponse(ResBody.error("waiting for search results too long").toString());
                    }
                }
            }
        }
        return NanoHTTPD.newFixedLengthResponse(ResBody.error("error request").toString());
    }

    public Object[] getSearchInfo() {
        Object[] info = new Object[3];
        info[0] = (Supplier<String>) () -> searchText;
        info[1] = (Supplier<String[]>) () -> searchCase;
        info[2] = (Supplier<String[]>) () -> keywords;
        return info;
    }

    public boolean isSearchInfoSet() {
        return this.isSearchInfoSet;
    }

    public void resetSearchInfo() {
        this.isSearchInfoSet = false;
    }

    public void setSearchResults(ConcurrentLinkedQueue<String> results) {
        searchResults = results;
    }

    private void setSearchInfo(String inputText) {
        final int i = inputText.lastIndexOf(':');
        if (i == -1) {
            searchText = inputText;
            searchCase = null;
        } else {
            if (inputText.length() - 1 > i) {
                final char c = inputText.charAt(i + 1);
                // 如 test;/C:/  test;/C:\  test;/D:  test;/D:;  则判断为搜索磁盘内的文件
                if (c == '/' || c == File.separatorChar || c == ' ' || c == ';') {
                    searchText = inputText;
                    searchCase = null;
                } else {
                    searchText = inputText.substring(0, i);
                    String[] tmpSearchCase = semicolon.split(inputText.substring(i + 1));
                    searchCase = new String[tmpSearchCase.length];
                    for (int j = 0; j < tmpSearchCase.length; j++) {
                        searchCase[j] = tmpSearchCase[j].trim();
                    }
                }
            } else {
                searchText = inputText;
                searchCase = null;
            }
        }
        keywords = semicolon.split(searchText);
    }
}
