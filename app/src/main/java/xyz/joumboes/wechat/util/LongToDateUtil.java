package xyz.joumboes.wechat.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LongToDateUtil {

    public String toData(Long millSec) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(millSec);
        return sdf.format(date);
    }

}
