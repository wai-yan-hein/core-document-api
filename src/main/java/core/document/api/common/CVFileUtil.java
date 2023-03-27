package core.document.api.common;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.StringUtils;

import java.io.File;
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

    public static String getFileExtension(FilePart filePart) {
        String fileName = filePart.filename();
        String[] parts = StringUtils.split(fileName, ".");
        assert parts != null;
        if (parts.length > 1) {
            return parts[parts.length - 1];
        } else {
            return "";
        }
    }
}
