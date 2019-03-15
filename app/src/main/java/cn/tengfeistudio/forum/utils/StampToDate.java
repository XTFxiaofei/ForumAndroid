package cn.tengfeistudio.forum.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StampToDate {

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s) {
//        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
//        Date date = new Date(lt);
//        res = simpleDateFormat.format(date);
//        return res;
        //前面的lt是秒数，先乘1000得到毫秒数，再转为java.util.Date类型
        java.util.Date dt = new Date(lt * 1000);
        String sDateTime = simpleDateFormat.format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
        return sDateTime;
    }

    public static Date getDate(long s) {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Long time = new Long(s);
//        String d = format.format(time);
//        try {
//            Date date = format.parse(d);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       //前面的s是秒数，先乘1000得到毫秒数，再转为java.util.Date类型
        java.util.Date dt = new Date(s * 1000);
        //String sDateTime = sdf.format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
        return dt;
    }

    public static String getStringDate(long s){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //前面的s是秒数，先乘1000得到毫秒数，再转为java.util.Date类型
        java.util.Date dt = new Date(s * 1000);
        String sDateTime = sdf.format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
        return sDateTime;
    }


    /**
     * 获取当前年份
     * @return
     */
    public static int getCurrentYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    /**
     * 当前月份
     * @return
     */
    public static int getCurrentMonth(){
        Calendar cal = Calendar.getInstance();
        int month=cal.get(Calendar.MONTH);
        return month;
    }

    /**
     * 获取当前学年
     * @return  2015-2016-1
     */
    public static String getCurrentSchoolYear(){
        Calendar cal=Calendar.getInstance();
        int year=cal.get(Calendar.YEAR);
        int month=cal.get(Calendar.MONTH);
        int se=1;
        //大于9月份小于3月份为第一学期
        if(month>=9 || month<3){
            se=1;
        }else{
            se=2;
        }
        String schoolyear=year+"-"+(year+1)+"-"+se;
        return schoolyear;
    }

}
