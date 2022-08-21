package file.engine.remote.utils.zip;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class FileZipUtil {

    private FileZipUtil() {
    }

    /**
     * 检查文件夹中文件的大小是否超过限制
     * @param dir 文件夹
     * @param maxSizeInBytes 最大允许大小
     * @return true如果文件夹中文件的大小未超过最大允许的大小
     * @throws IOException exception
     */
    public static boolean checkFilesSize(String dir, long maxSizeInBytes) throws IOException {
        LinkedList<File> paths = new LinkedList<>();
        paths.add(new File(dir));
        long totalBytes = 0;
        do {
            File poll = paths.poll();
            if (poll.isDirectory()) {
                File[] files = poll.listFiles();
                if (files == null || files.length == 0) {
                    continue;
                }
                for (File file : files) {
                    totalBytes += Files.size(file.toPath());
                    if (totalBytes > maxSizeInBytes) {
                        return false;
                    }
                }
            } else {
                totalBytes += Files.size(poll.toPath());
                if (totalBytes > maxSizeInBytes) {
                    return false;
                }
            }
        } while (!paths.isEmpty());
        return true;
    }

    /**
     * sourceFile一定要是文件夹
     * 默认会在同目录下生成zip文件
     */
    public static void fileToZip(String sourceFilePath, String fileName) throws Exception {
        fileToZip(new File(sourceFilePath), fileName);
    }

    /**
     * sourceFile一定要是文件夹
     * 默认会在同目录下生成zip文件
     */
    public static void fileToZip(File sourceFile, String fileName) throws Exception {
        if (!sourceFile.exists()) {
            throw new RuntimeException("不存在");
        }
        if (!sourceFile.isDirectory()) {
            throw new RuntimeException("不是文件夹");
        }
        //zip文件生成位置
        File zipFile = new File(fileName);
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
        try (fos; zos) {
            fileToZip(zos, sourceFile, "");
        }
    }


    private static void fileToZip(ZipOutputStream zos, File sourceFile, String path) throws Exception {
        //如果是文件夹只创建zip实体即可，如果是文件，创建zip实体后还要读取文件内容并写入
        if (sourceFile.isDirectory()) {
            path = path + sourceFile.getName() + "/";
            ZipEntry zipEntry = new ZipEntry(path);
            zos.putNextEntry(zipEntry);
            File[] files = sourceFile.listFiles();
            if (null != files && files.length != 0) {
                for (File file : files) {
                    fileToZip(zos, file, path);
                }
            }
        } else {
            //创建ZIP实体，并添加进压缩包
            ZipEntry zipEntry = new ZipEntry(path + sourceFile.getName());
            zos.putNextEntry(zipEntry);
            byte[] bufs = new byte[1024 * 10];
            //读取待压缩的文件并写进压缩包里
            FileInputStream fis = new FileInputStream(sourceFile);
            BufferedInputStream bis = new BufferedInputStream(fis, 1024 * 10);
            try (fis; bis) {
                int read;
                while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
                    zos.write(bufs, 0, read);
                }
            }
        }
    }

}