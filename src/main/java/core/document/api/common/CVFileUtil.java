package core.document.api.common;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

public class CVFileUtil {
    public static final int FOLDER = 1;
    public static final int FILE = 2;
    public static final String ROOT = "data";


    public static boolean createFolderOS(String path) {
        File folder = new File(Paths.get(path).toString());
        return folder.mkdirs();
    }

    public static boolean isEmpty(String path) {
        File folder = new File(Paths.get(path).toString());
        return !folder.exists();
    }

    public static String getFileExtension(FilePart file) {
        String fileName = file.name();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
}
