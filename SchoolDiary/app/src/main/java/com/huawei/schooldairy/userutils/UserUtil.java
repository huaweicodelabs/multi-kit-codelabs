/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.schooldairy.userutils;

import android.util.Log;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Shared Preference methods which is used in this whole application
 * @author: Huawei
 * @since: 25-05-2021
 */
public class UserUtil {

    private static final String CLOUD_DB_FORMAT = "yyyy-MM-dd HH:mm:ss SSS";
    private static final String LOCAL_FORMAT = "dd/MM/yyyy HH:mm";

    /**
     * Get Current UTC time
     * @return UTC time as {@link String}
     */
    public static String getCurrentDateTimeStringAsUTC() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CLOUD_DB_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date);
    }

    /**g
     * Get current UTC time
     * @return UTC time as {@link Date}
     */
    public static Date getCurrentDateTimeAsUTC() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CLOUD_DB_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return date;
    }

    /**
     * Convert {@link Date} to {@link String} as Cloud DB format
     * @param date date need to be converted as String
     * @return as {@link String}
     */
    public static String dateToString(@NonNull Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(CLOUD_DB_FORMAT);
        String dateTime = dateFormat.format(date);
        System.out.println("Current Date Time : " + dateTime);
        return dateTime;
    }

    /**
     * Convert {@link Date} to {@link String} as Local format to Display
     * @param date date need to be converted as String
     * @return as {@link String}
     */
    public static String dateToStringDisplay(@NonNull Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(LOCAL_FORMAT);
        String dateTime = dateFormat.format(date);
        System.out.println("Current Date Time : " + dateTime);
        return dateTime;
    }

    /**
     * Convert local date {@link String} to UTC {@link Date}
     * @param datesToConvert need to converted as Date
     * @return as {@link Date}
     */
    public static Date localToUTCDate(String datesToConvert) {
        String dateToReturn = datesToConvert;
        SimpleDateFormat sdf = new SimpleDateFormat(CLOUD_DB_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault());
        Date gmt = null;
        SimpleDateFormat sdfOutPutToSend = new SimpleDateFormat(CLOUD_DB_FORMAT);
        sdfOutPutToSend.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            gmt = sdf.parse(datesToConvert);
            //dateToReturn = sdfOutPutToSend.format(gmt);
        } catch (ParseException e) {
            Log.e("Error", e.getMessage());
        }
        return gmt;
    }

    /**
     * Convert local date {@link String} to UTC {@link Date}
     * @param datesToConvert need to converted as Date
     * @return as {@link Date}
     */
    public static String utcToLocalString(String datesToConvert) {
        String dateToReturn = datesToConvert;
        SimpleDateFormat sdf = new SimpleDateFormat(CLOUD_DB_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gmt = null;
        SimpleDateFormat sdfOutPutToSend = new SimpleDateFormat(LOCAL_FORMAT);
        sdfOutPutToSend.setTimeZone(TimeZone.getDefault());
        try {
            gmt = sdf.parse(datesToConvert);
            dateToReturn = sdfOutPutToSend.format(gmt);

        } catch (ParseException e) {
            Log.e("Error", e.getMessage());
        }
        return dateToReturn;
    }

    /**
     * To display user id with hidden characters
     * @param string to be display
     * @return string with hidden characters
     */
    public static String hideInfoWithStar(String string){
        int len = string.length();
        return string.replace(string.substring(3, len - 4), "*****");
    }
}
