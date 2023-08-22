package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    private FileUtil() {

    }

    public static byte[] readFileBytes(String filePath) throws IOException {
        byte[] fileBytes = null;
        // 读取V1 CA证书
        try (FileInputStream is = new FileInputStream(filePath)) {
            int fileSize = is.available();
            fileBytes = new byte[fileSize];

            int ret = is.read(fileBytes);
            if (ret != fileSize) {
                throw new IOException("read file error:" + ret);
            }
        }
        return fileBytes;
    }

    /**
     * @Author：
     * @Description：获取某个目录下所有直接下级文件，不包括目录下的子目录的下的文件，所以不用递归获取
     * @Date：
     */
    public static List<String> getFiles(String path) {
        List<String> files = new ArrayList<>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                files.add(tempList[i].toString());
            }
            if (tempList[i].isDirectory()) {
                //这里就不递归了，
            }
        }
        return files;
    }
}
