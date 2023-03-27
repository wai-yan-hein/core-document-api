package core.document.api.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Util1 {
    public static boolean isNullOrEmpty(Object obj) {
        return obj == null || obj.toString().isEmpty();
    }

    public static Date getTodayDate() {
        return Calendar.getInstance().getTime();
    }

    public static String toDateStr(Date date, String format) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat f = new SimpleDateFormat(format);
        return f.format(date);
    }
}
