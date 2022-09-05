package com.huawei.schooldairy.userutils;

import androidx.annotation.NonNull;

import com.huawei.agconnect.cloud.database.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    //From String to Date
    public static Date stringToDate(@NonNull String strDate) {
        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            date = format.parse(strDate);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    //From Date to String
    public static String dateToString(@NonNull Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateTime = dateFormat.format(date);
        System.out.println("Current Date Time : " + dateTime);
        return dateTime;
    }
}
