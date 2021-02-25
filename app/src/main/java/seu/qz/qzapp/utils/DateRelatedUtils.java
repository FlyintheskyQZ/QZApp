package seu.qz.qzapp.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

public class DateRelatedUtils {

    private static Calendar calendar = Calendar.getInstance();
    public static final int TYPE_ORIGINAL = 0;
    public static final int TYPE_CHINESE = 1;
    public static final int TYPE_CACHE = 2;
    public static final int TYPE_SHORT = 3;

    private static DateFormat format_Original = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static DateFormat format_Chinese = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
    private static DateFormat format_CacheFile = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
    private static DateFormat format_Short = new SimpleDateFormat("yyyy-MM-dd-hh-mm");

    /**
     *
     * @param time：形式如“1：2：3”代表一个小时两分三秒
     * @return
     */
    public static Date getDateByString(String time){
        String[] times = time.split(":");
        calendar.set(0, 0, 0, Integer.parseInt(times[0]), Integer.parseInt(times[1]), Integer.parseInt(times[2]));
        return  calendar.getTime();
    }

    public static boolean nearTime(Date now, Date begin, float time_hour){
        long now_time = now.getTime();
        long begin_time = begin.getTime();
        if(now_time > begin_time){
            return false;
        }
        if((begin_time - now_time)/(1000 * 60 * 60) < time_hour){
            return true;
        }else {
            return false;
        }
    }

    //已特定格式显示时间
    public static String FormatOutputByDate(Date date, int Type){
        if(date == null){
            date = new Date(System.currentTimeMillis());
        }
        String time_expression = null;
        switch (Type){
            case TYPE_ORIGINAL:
                time_expression = format_Original.format(date);
                break;
            case TYPE_CHINESE:
                time_expression = format_Chinese.format(date);
                break;
            case TYPE_CACHE:
                time_expression = format_CacheFile.format(date);
                break;
            case TYPE_SHORT:
                time_expression = format_Short.format(date);
            default:
                time_expression = format_Original.format(date);
                break;
        }

        return time_expression;
    }

    public static Date formDateByString(String time, int Type){
         if(time == null || time.isEmpty()){
             return new Date(System.currentTimeMillis());
         }
         Date date = null;
         try{
             switch (Type){
                 case TYPE_ORIGINAL:
                     date = format_Original.parse(time);
                     break;
                 case TYPE_CHINESE:
                    date = format_Chinese.parse(time);
                     break;
                 case TYPE_SHORT:
                     date = format_Short.parse(time);
                 default:break;
             }
             return date;
         }catch (Exception e){
             e.printStackTrace();
             return new Date(System.currentTimeMillis());
        }
    }

    private static boolean sameDate(Date d1, Date d2) {
        LocalDate localDate1 = ZonedDateTime.ofInstant(d1.toInstant(), ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = ZonedDateTime.ofInstant(d2.toInstant(), ZoneId.systemDefault()).toLocalDate();
        return localDate1.isEqual(localDate2);
    }
}
