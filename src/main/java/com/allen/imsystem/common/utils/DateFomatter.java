package com.allen.imsystem.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFomatter {

    private static String[] day_of_week = {"","星期日","星期一","星期二","星期三","星期四","星期五","星期六"};

    public static String formatMessageDate(Date msgDate){
        Date nowDate = new Date();
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(nowDate);
        Calendar msgCalendar = Calendar.getInstance();
        msgCalendar.setTime(msgDate);

        if(nowCalendar.get(Calendar.ERA) == msgCalendar.get(Calendar.ERA)
                && nowCalendar.get(Calendar.YEAR)==msgCalendar.get(Calendar.YEAR)){

            int lessDay = nowCalendar.get(Calendar.DAY_OF_YEAR) - msgCalendar.get(Calendar.DAY_OF_YEAR);
            if(lessDay == 0){// 同一天
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                return simpleDateFormat.format(msgDate);
            }else if(lessDay == 1){ // 昨天
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                return "昨天 " + simpleDateFormat.format(msgDate);
            }else if(lessDay < 5){  // 2-4天前
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                return day_of_week[msgCalendar.get(7)] +" "+ simpleDateFormat.format(msgDate);
            }else{  // 5天以上
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd HH:mm");
                return simpleDateFormat.format(msgDate);
            }
        }else{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd HH:mm");
            return simpleDateFormat.format(msgDate);
        }
    }


    public static String formatChatSessionDate(Date msgDate){
        if(msgDate == null){
            return "";
        }
        Date nowDate = new Date();
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(nowDate);
        Calendar msgCalendar = Calendar.getInstance();
        msgCalendar.setTime(msgDate);

        if(nowCalendar.get(0) == msgCalendar.get(0) && nowCalendar.get(1)==msgCalendar.get(1)){
            int lessDay = nowCalendar.get(6) - msgCalendar.get(6);
            if(lessDay == 0){// 同一天
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                return simpleDateFormat.format(msgDate);
            }else if(lessDay == 1){ // 昨天
                return "昨天";
            }else if(lessDay < 5){  // 2-4天前
                return day_of_week[msgCalendar.get(7)];
            }else{
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd");
                return simpleDateFormat.format(msgDate);
            }
        }else{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd");
            return simpleDateFormat.format(msgDate);
        }

    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        // 判断是否是同一个实际的同一年的同为第N天
        if(cal1 != null && cal2 != null) {
            return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    /**
     * 大的日期在后，小的日期在前
     * @param cal1
     * @param cal2
     * @return
     */
    public static boolean isYesterday(Calendar cal1, Calendar cal2) {

        if(cal1 != null && cal2 != null) {
            return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6)-1;
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }


}
