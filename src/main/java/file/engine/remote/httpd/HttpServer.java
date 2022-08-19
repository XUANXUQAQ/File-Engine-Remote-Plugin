package file.engine.remote.httpd;

import fi.iki.elonen.NanoHTTPD;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
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
    private static final HashMap<String, String> suffixMimeMap = new HashMap<>();

    static {
        suffixMimeMap.put("ez", "application/andrew-inset");
        suffixMimeMap.put("hqx", "application/mac-binhex40");
        suffixMimeMap.put("cpt", "application/mac-compactpro");
        suffixMimeMap.put("doc", "application/msword");
        suffixMimeMap.put("bin", "application/octet-stream");
        suffixMimeMap.put("dms", "application/octet-stream");
        suffixMimeMap.put("lha", "application/octet-stream");
        suffixMimeMap.put("lzh", "application/octet-stream");
        suffixMimeMap.put("exe", "application/octet-stream");
        suffixMimeMap.put("class", "application/octet-stream");
        suffixMimeMap.put("so", "application/octet-stream");
        suffixMimeMap.put("dll", "application/octet-stream");
        suffixMimeMap.put("oda", "application/oda");
        suffixMimeMap.put("pdf", "application/pdf");
        suffixMimeMap.put("ai", "application/postscript");
        suffixMimeMap.put("eps", "application/postscript");
        suffixMimeMap.put("ps", "application/postscript");
        suffixMimeMap.put("smi", "application/smil");
        suffixMimeMap.put("smil", "application/smil");
        suffixMimeMap.put("mif", "application/vnd.mif");
        suffixMimeMap.put("xls", "application/vnd.ms-excel");
        suffixMimeMap.put("ppt", "application/vnd.ms-powerpoint");
        suffixMimeMap.put("wbxml", "application/vnd.wap.wbxml");
        suffixMimeMap.put("wmlc", "application/vnd.wap.wmlc");
        suffixMimeMap.put("wmlsc", "application/vnd.wap.wmlscriptc");
        suffixMimeMap.put("bcpio", "application/x-bcpio");
        suffixMimeMap.put("vcd", "application/x-cdlink");
        suffixMimeMap.put("pgn", "application/x-chess-pgn");
        suffixMimeMap.put("cpio", "application/x-cpio");
        suffixMimeMap.put("csh", "application/x-csh");
        suffixMimeMap.put("dcr", "application/x-director");
        suffixMimeMap.put("dir", "application/x-director");
        suffixMimeMap.put("dxr", "application/x-director");
        suffixMimeMap.put("dvi", "application/x-dvi");
        suffixMimeMap.put("spl", "application/x-futuresplash");
        suffixMimeMap.put("gtar", "application/x-gtar");
        suffixMimeMap.put("hdf", "application/x-hdf");
        suffixMimeMap.put("js", "application/x-javascript");
        suffixMimeMap.put("skp", "application/x-koan");
        suffixMimeMap.put("skd", "application/x-koan");
        suffixMimeMap.put("skt", "application/x-koan");
        suffixMimeMap.put("skm", "application/x-koan");
        suffixMimeMap.put("latex", "application/x-latex");
        suffixMimeMap.put("nc", "application/x-netcdf");
        suffixMimeMap.put("cdf", "application/x-netcdf");
        suffixMimeMap.put("sh", "application/x-sh");
        suffixMimeMap.put("shar", "application/x-shar");
        suffixMimeMap.put("swf", "application/x-shockwave-flash");
        suffixMimeMap.put("sit", "application/x-stuffit");
        suffixMimeMap.put("sv4cpio", "application/x-sv4cpio");
        suffixMimeMap.put("sv4crc", "application/x-sv4crc");
        suffixMimeMap.put("tar", "application/x-tar");
        suffixMimeMap.put("tcl", "application/x-tcl");
        suffixMimeMap.put("tex", "application/x-tex");
        suffixMimeMap.put("texinfo", "application/x-texinfo");
        suffixMimeMap.put("texi", "application/x-texinfo");
        suffixMimeMap.put("t", "application/x-troff");
        suffixMimeMap.put("tr", "application/x-troff");
        suffixMimeMap.put("roff", "application/x-troff");
        suffixMimeMap.put("man", "application/x-troff-man");
        suffixMimeMap.put("me", "application/x-troff-me");
        suffixMimeMap.put("ms", "application/x-troff-ms");
        suffixMimeMap.put("ustar", "application/x-ustar");
        suffixMimeMap.put("src", "application/x-wais-source");
        suffixMimeMap.put("xhtml", "application/xhtml+xml");
        suffixMimeMap.put("xht", "application/xhtml+xml");
        suffixMimeMap.put("zip", "application/zip");
        suffixMimeMap.put("au", "audio/basic");
        suffixMimeMap.put("snd", "audio/basic");
        suffixMimeMap.put("mid", "audio/midi");
        suffixMimeMap.put("midi", "audio/midi");
        suffixMimeMap.put("kar", "audio/midi");
        suffixMimeMap.put("mpga", "audio/mpeg");
        suffixMimeMap.put("mp2", "audio/mpeg");
        suffixMimeMap.put("mp3", "audio/mpeg");
        suffixMimeMap.put("aif", "audio/x-aiff");
        suffixMimeMap.put("aiff", "audio/x-aiff");
        suffixMimeMap.put("aifc", "audio/x-aiff");
        suffixMimeMap.put("m3u", "audio/x-mpegurl");
        suffixMimeMap.put("ram", "audio/x-pn-realaudio");
        suffixMimeMap.put("rm", "audio/x-pn-realaudio");
        suffixMimeMap.put("rpm", "audio/x-pn-realaudio-plugin");
        suffixMimeMap.put("ra", "audio/x-realaudio");
        suffixMimeMap.put("wav", "audio/x-wav");
        suffixMimeMap.put("pdb", "chemical/x-pdb");
        suffixMimeMap.put("xyz", "chemical/x-xyz");
        suffixMimeMap.put("bmp", "image/bmp");
        suffixMimeMap.put("gif", "image/gif");
        suffixMimeMap.put("ief", "image/ief");
        suffixMimeMap.put("jpeg", "image/jpeg");
        suffixMimeMap.put("jpg", "image/jpeg");
        suffixMimeMap.put("jpe", "image/jpeg");
        suffixMimeMap.put("png", "image/png");
        suffixMimeMap.put("tiff", "image/tiff");
        suffixMimeMap.put("tif", "image/tiff");
        suffixMimeMap.put("djvu", "image/vnd.djvu");
        suffixMimeMap.put("djv", "image/vnd.djvu");
        suffixMimeMap.put("wbmp", "image/vnd.wap.wbmp");
        suffixMimeMap.put("ras", "image/x-cmu-raster");
        suffixMimeMap.put("pnm", "image/x-portable-anymap");
        suffixMimeMap.put("pbm", "image/x-portable-bitmap");
        suffixMimeMap.put("pgm", "image/x-portable-graymap");
        suffixMimeMap.put("ppm", "image/x-portable-pixmap");
        suffixMimeMap.put("rgb", "image/x-rgb");
        suffixMimeMap.put("xbm", "image/x-xbitmap");
        suffixMimeMap.put("xpm", "image/x-xpixmap");
        suffixMimeMap.put("xwd", "image/x-xwindowdump");
        suffixMimeMap.put("igs", "model/iges");
        suffixMimeMap.put("iges", "model/iges");
        suffixMimeMap.put("msh", "model/mesh");
        suffixMimeMap.put("mesh", "model/mesh");
        suffixMimeMap.put("silo", "model/mesh");
        suffixMimeMap.put("wrl", "model/vrml");
        suffixMimeMap.put("vrml", "model/vrml");
        suffixMimeMap.put("css", "text/css");
        suffixMimeMap.put("html", "text/html");
        suffixMimeMap.put("htm", "text/html");
        suffixMimeMap.put("asc", "text/plain");
        suffixMimeMap.put("txt", "text/plain");
        suffixMimeMap.put("rtx", "text/richtext");
        suffixMimeMap.put("rtf", "text/rtf");
        suffixMimeMap.put("sgml", "text/sgml");
        suffixMimeMap.put("sgm", "text/sgml");
        suffixMimeMap.put("tsv", "text/tab-separated-values");
        suffixMimeMap.put("wml", "text/vnd.wap.wml");
        suffixMimeMap.put("wmls", "text/vnd.wap.wmlscript");
        suffixMimeMap.put("etx", "text/x-setext");
        suffixMimeMap.put("xsl", "text/xml");
        suffixMimeMap.put("xml", "text/xml");
        suffixMimeMap.put("mpeg", "video/mpeg");
        suffixMimeMap.put("mpg", "video/mpeg");
        suffixMimeMap.put("mpe", "video/mpeg");
        suffixMimeMap.put("qt", "video/quicktime");
        suffixMimeMap.put("mov", "video/quicktime");
        suffixMimeMap.put("mxu", "video/vnd.mpegurl");
        suffixMimeMap.put("avi", "video/x-msvideo");
        suffixMimeMap.put("movie", "video/x-sgi-movie");
        suffixMimeMap.put("ice", "x-conference/x-cooltalk");
        suffixMimeMap.put("form", "application/x-www-form-urlencoded");
        suffixMimeMap.put("svg", "image/svg+xml");
        suffixMimeMap.put("ico", "image/x-icon");
    }

    public HttpServer(int port) throws IOException {
        super(port);
        start(SOCKET_READ_TIMEOUT, false);
    }

    /**
     * 处理网络请求，当请求为post且请求地址为/search，则设置搜索关键字并发起搜索事件
     * 否则作为web资源服务器，向前端返回静态资源
     *
     * @return Response
     */
    @SneakyThrows
    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        if (Method.POST.equals(method) && "/search".equals(uri)) {
            Map<String, List<String>> parameters = session.getParameters();
            List<String> inputTextList = parameters.get("inputText");
            if (!inputTextList.isEmpty()) {
                String inputText = inputTextList.get(0);
                if (inputText.length() < 300) {
                    setSearchInfo(inputText);
                    searchResults = null;
                    isSearchInfoSet = true;
                    final long startWaitingTime = System.currentTimeMillis();
                    while (searchResults == null && System.currentTimeMillis() - startWaitingTime < 3000)
                        Thread.onSpinWait();
                    if (searchResults != null) {
                        return NanoHTTPD.newFixedLengthResponse(ResBody.success(null, 0).toString());
                    } else {
                        return NanoHTTPD.newFixedLengthResponse(ResBody.error("waiting for search results too long").toString());
                    }
                }
            }
        } else if (Method.GET.equals(method) && "/results".equals(uri)) {
            // 获取结果
            Map<String, List<String>> parameters = session.getParameters();
            List<String> pageNumList = parameters.get("pageNum");
            List<String> pageSizeList = parameters.get("pageSize");
            if (!pageNumList.isEmpty() && !pageSizeList.isEmpty()) {
                final int pageNum = Integer.parseInt(pageNumList.get(0));
                final int pageSize = Integer.parseInt(pageSizeList.get(0));
                ArrayList<String> results = new ArrayList<>(searchResults);
                ArrayList<String> retArray = new ArrayList<>();
                final int size = results.size();
                final int pages = (int) Math.ceil((double) size / pageSize);
                for (int i = (pageNum - 1) * pageSize; i < pageNum * pageSize && i < size; ++i) {
                    retArray.add(results.get(i));
                }
                return NanoHTTPD.newFixedLengthResponse(ResBody.success(retArray, pages).toString());
            }
        } else {
            String mime = selectMime(uri.substring(uri.lastIndexOf('.') + 1));
            StringBuilder res = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(HttpServer.class.getResourceAsStream(uri))))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    res.append(line);
                }
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mime, res.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return NanoHTTPD.newFixedLengthResponse(ResBody.error("error request").toString());
    }

    /**
     * 获得搜索关键字及过滤模式
     *
     * @return object
     */
    public Object[] getSearchInfo() {
        Object[] info = new Object[3];
        info[0] = (Supplier<String>) () -> searchText;
        info[1] = (Supplier<String[]>) () -> searchCase;
        info[2] = (Supplier<String[]>) () -> keywords;
        return info;
    }

    /**
     * 是否已经成功解析搜索关键字
     *
     * @return true如果搜索关键字解析完成，可以发送搜索事件
     */
    public boolean isSearchInfoSet() {
        return this.isSearchInfoSet;
    }

    /**
     * 重置搜索关键字解析
     */
    public void resetSearchInfo() {
        this.isSearchInfoSet = false;
    }

    /**
     * 设置搜索结果
     *
     * @param results 搜索结果
     */
    public void setSearchResults(ConcurrentLinkedQueue<String> results) {
        searchResults = results;
    }

    /**
     * 设置搜索的关键字
     *
     * @param inputText 用户输入
     */
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

    /**
     * 选择文件对应的content type
     *
     * @param suffix 后缀
     * @return content type
     */
    private String selectMime(String suffix) {
        return suffixMimeMap.get(suffix);
    }
}
