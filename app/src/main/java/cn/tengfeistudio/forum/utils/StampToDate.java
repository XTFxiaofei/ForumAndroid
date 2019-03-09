package cn.tengfeistudio.forum.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StampToDate {

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    public static Date getDate(long s) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long time = new Long(s);
        String d = format.format(time);
        try {
            Date date = format.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
