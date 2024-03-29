package file.engine.remote;

import file.engine.remote.httpd.HttpServer;
import file.engine.remote.utils.ColorUtils;
import file.engine.remote.utils.OpenUtil;
import file.engine.remote.utils.VersionUtil;
import file.engine.remote.utils.configs.ConfigsUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class PluginMain extends Plugin {

    private Color backgroundColor;
    private Color labelChosenColor;
    private Color labelDefaultFontColor;
    private Color labelChosenFontColor;
    private HttpServer httpServer;
    private final String commandSet = ">set";
    private final String commandOpen = ">open";

    /**
     * 当用户修改FIle-Engine的设置后，将调用此函数。
     *
     * @param configs configs
     */
    @Override
    public void configsChanged(Map<String, Object> configs) {
        backgroundColor = new Color((Integer) configs.get("defaultBackground"));
        labelChosenColor = new Color((Integer) configs.get("labelColor"));
        labelDefaultFontColor = new Color((Integer) configs.get("fontColor"));
        labelChosenFontColor = new Color((Integer) configs.get("fontColorWithCoverage"));
    }

    /**
     * 当搜索栏文本更改时，将调用此函数。
     *
     * @param text Example : 当在搜索框输入 "&gt;examplePlugin TEST" 时, 参数text值为 "TEST"
     */
    @Override
    public void textChanged(String text) {
        if (commandSet.equals(text)) {
            try {
                OpenUtil.openFolderByExplorer(ConfigsUtil.CONFIGURATION_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (commandOpen.equals(text)) {
            try {
                OpenUtil.openBrowser(String.format("http://localhost:%d/index.html", ConfigsUtil.getInstance().getPort()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (text != null && text.length() > 1) {
                text = text.substring(1);
                if (commandSet.substring(1).startsWith(text)) {
                    addToResultQueue(commandSet);
                } else if (commandOpen.substring(1).startsWith(text)) {
                    addToResultQueue(commandOpen);
                } else {
                    addToResultQueue(commandSet);
                    addToResultQueue(commandOpen);
                }
            } else {
                addToResultQueue(commandSet);
                addToResultQueue(commandOpen);
            }
        }
    }

    /**
     * 启动File-Engine时，将调用该函数。
     * 您可以在此处初始化插件
     */
    @Override
    public void loadPlugin(Map<String, Object> configs) throws RuntimeException {
        try {
            VersionUtil.registerDownloadListener();
            HashMap<String, Class<?>> startSearchEventFields = new HashMap<>();
            startSearchEventFields.put("searchCase", Supplier.class);
            startSearchEventFields.put("searchText", Supplier.class);
            startSearchEventFields.put("keywords", Supplier.class);
            checkEvent("file.engine.event.handler.impl.database.StartSearchEvent", startSearchEventFields);

            boolean isCoreExist = false;
            try {
                checkEvent("file.engine.event.handler.impl.database.StartCoreEvent", Collections.emptyMap());
                isCoreExist = true;
            } catch (Exception e) {
                HashMap<String, Class<?>> searchDoneEventFields = new HashMap<>();
                searchDoneEventFields.put("searchResults", ConcurrentLinkedQueue.class);
                checkEvent("file.engine.event.handler.impl.database.SearchDoneEvent", searchDoneEventFields);
            }
            backgroundColor = new Color((Integer) configs.get("defaultBackground"));
            labelChosenColor = new Color((Integer) configs.get("labelColor"));
            labelDefaultFontColor = new Color((Integer) configs.get("fontColor"));
            labelChosenFontColor = new Color((Integer) configs.get("fontColorWithCoverage"));
            ConfigsUtil configsUtil = ConfigsUtil.getInstance();
            httpServer = new HttpServer(configsUtil.getPort(), isCoreExist);
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 当File-Engine关闭时，将调用该函数。
     */
    @Override
    public void unloadPlugin() {
        httpServer.stop();
    }

    /**
     * 当File-Engine进入插件模式后，每释放一次键盘该方法将会执行一次。有关按键释放事件的定义，请参见 swing KeyEvent 的类描述。
     * 请注意：由于上下键用于切换显示结果，所以按下键盘上下键不会触发该方法(key code 38和40不会触发)。
     *
     * @param e      KeyEvent, Which key on the keyboard is released.
     * @param result Currently selected content.
     */
    @Override
    public void keyReleased(KeyEvent e, String result) {

    }

    /**
     * 当File-Engine进入插件模式后，每按下一次键盘该方法将会执行一次。有关按键按下事件的定义，请参见 swing KeyEvent 的类描述。
     * 请注意：由于上下键用于切换显示结果，所以按下键盘上下键不会触发该方法(key code 38和40不会触发)。
     *
     * @param e      KeyEvent, Which key on the keyboard is pressed.
     * @param result Currently selected content.
     */
    @Override
    public void keyPressed(KeyEvent e, String result) {
        if (e.getKeyCode() == 10) {
            if (commandSet.equals(result)) {
                try {
                    OpenUtil.openFolderByExplorer(ConfigsUtil.CONFIGURATION_FILE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else if (commandOpen.equals(result)) {
                try {
                    OpenUtil.openBrowser(String.format("http://localhost:%d/index.html", ConfigsUtil.getInstance().getPort()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 当File-Engine进入插件模式后，每按下和释放一次键盘该方法将会执行一次。有关按键事件的定义，请参见 swing KeyEvent 的类描述。
     * 请注意：由于上下键用于切换显示结果，所以按下键盘上下键不会触发该方法(key code 38和40不会触发)。
     *
     * @param e      KeyEvent, Which key on the keyboard is pressed.
     * @param result Currently selected content.
     */
    @Override
    public void keyTyped(KeyEvent e, String result) {

    }

    /**
     * 当File-Engine进入插件模式后，鼠标每点击窗口以下该方法将会执行一次。有关鼠标事件的定义，请参见swing MouseEvent的类描述。
     *
     * @param e      Mouse event
     * @param result Currently selected content.
     */
    @Override
    public void mousePressed(MouseEvent e, String result) {

    }

    /**
     * 当File-Engine进入插件模式后，鼠标每释放一次该方法将会执行一次。有关鼠标事件的定义，请参见swing MouseEvent的类描述。
     *
     * @param e      Mouse event
     * @param result Currently selected content
     */
    @Override
    public void mouseReleased(MouseEvent e, String result) {

    }

    /**
     * 当File-Engine的搜索框被打开该方法将会被调用一次。并不需要进入插件模式。
     *
     * @param showingMode 显示模式
     *                    <p>
     *                    目前File-Engine有两种模式：普通显示和贴靠资源管理器显示，对应的showingMode为 NORMAL_SHOWING， EXPLORER_ATTACH
     */
    @Override
    public void searchBarVisible(String showingMode) {

    }

    /**
     * 获取插件图标。它可以是png，jpg。
     * 尽量让图标变小，否则会占用太多内存。
     *
     * @return icon
     */
    @Override
    public ImageIcon getPluginIcon() {
        return new ImageIcon(Objects.requireNonNull(PluginMain.class.getResource("/remote.png")));
    }

    /**
     * 获取插件的官方网站。
     *
     * @return official site
     */
    @Override
    public String getOfficialSite() {
        return "https://github.com/XUANXUQAQ/File-Engine-Remote-Plugin";
    }

    /**
     * 获取插件版本信息
     *
     * @return version
     */
    @Override
    public String getVersion() {
        return VersionUtil._getPluginVersion();
    }

    /**
     * 获取插件描述，插件的介绍信息以及使用方法。插件的介绍信息将会显示在设置界面。
     * 只需在外部写入描述，并将其粘贴然后return。
     *
     * @return description
     */
    @Override
    public String getDescription() {
        return "这是一个File-Engine远程搜索插件，通过这个插件，你可以通过浏览器搜索本地文件，并进行下载。\n" +
                "图标来自： https://icons8.com/icon/yUvFSXKGhJca/云访问  " +
                "icon by https://icons8.com";
    }

    /**
     * 检查当前版本是否为最新版本。
     *
     * @return true or false
     * @see #getUpdateURL()
     */
    @Override
    @SuppressWarnings({"unused", "RedundantThrows"})
    public boolean isLatest() throws Exception {
        return VersionUtil._isLatest();
    }

    /**
     * 返回下载插件jar文件的url
     * 当**isLatest**返回false，将会弹出窗口询问用户是否更新，如果用户点击确定，则会调用该方法来下载新版本的插件。
     *
     * @return download url
     * @see #isLatest()
     */
    @Override
    public String getUpdateURL() {
        return VersionUtil._getUpdateURL();
    }

    /**
     * 当插件返回了结果到File-Engine，File-Engine尝试显示时，将会调用该方法。
     *
     * @param result   current selected content.
     * @param label    需要显示的JLabel.
     * @param isChosen 如果当前label是目前被用户选中的，您应该将标签设置为不同的背景，背景颜色可以通过loadPlugins和configsChanged方法的参数获得。
     *                 <p>
     *                 您只能设置JLabel的图标、文本和背景，请不要设置其他属性，如border，name以及其他
     */
    @Override
    public void showResultOnLabel(String result, JLabel label, boolean isChosen) {
        String defaultFontColor = "#" + ColorUtils.parseColorHex(labelDefaultFontColor);
        String fontColorHighLight = "#" + ColorUtils.parseColorHex(labelChosenFontColor);
        String html = "<html><body><span%s>%s</span></body></html>";
        if (commandSet.equals(result)) {
            result = result + "<br>" + "打开设置文件位置";
        } else if (commandOpen.equals(result)) {
            result = result + "<br>" + "打开网页";
        }
        html = String.format(html, String.format(" style=\"color: %s\"", isChosen ? fontColorHighLight : defaultFontColor), result);
        label.setText(html);
        if (isChosen) {
            label.setBackground(labelChosenColor);
        } else {
            label.setBackground(backgroundColor);
        }
    }

    /**
     * 获取插件的作者名，将会显示在插件的设置界面。
     *
     * @return author name
     */
    @Override
    public String getAuthor() {
        return "XUANXU";
    }


    /**
     * File-Engine中拥有一个事件处理系统，每一个事件被处理，该方法将会被调用一次。你可以在此处监听主程序当前处理了什么事件。也可以通过该接口实现多个插件互相通信。
     * 你可以通过 sendEventToFileEngine() 方法发送事件
     *
     * @param c             事件类
     * @param eventInstance 事件实例
     * @see #sendEventToFileEngine(String, Object...)
     * @see #sendEventToFileEngine(Event)
     */
    @Override
    @Deprecated
    public void eventProcessed(Class<?> c, Object eventInstance) {
    }

    public void openSettings() {
        try {
            OpenUtil.openFolderByExplorer(ConfigsUtil.CONFIGURATION_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
