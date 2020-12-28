package com.ulab.motionapp.common;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Performs the Date format operation.
 */
public class DateUtils {
    // guillaume : All of them are unused
    public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DD_MM_YYYY = "dd-MM-yyyy";
    public static final String DATE_FORMAT_MM_DD_YYYY = "MM-dd-yyyy";

    // guillaume : unused
    public static String convertDate(final String strSourceFormat, final String strDestinationFormat, final String sourceDate) {
        final SimpleDateFormat sourceFormat = new SimpleDateFormat(strSourceFormat, Locale.getDefault());
        final SimpleDateFormat DesiredFormat = new SimpleDateFormat(strDestinationFormat, Locale.getDefault());

        final Date date;
        try {
            date = sourceFormat.parse(sourceDate);

            return DesiredFormat.format(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    // guillaume : unused
    public static String convertDate(final String strDestinationFormat, final Calendar calendar) {
        final SimpleDateFormat sourceFormat = new SimpleDateFormat(strDestinationFormat, Locale.getDefault());
        return sourceFormat.format(calendar.getTime());
    }

    public static String diffInHHMMSS(long diff) {
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = diff / daysInMilli;
        diff = diff % daysInMilli;

        long elapsedHours = diff / hoursInMilli;
        diff = diff % hoursInMilli;

        long elapsedMinutes = diff / minutesInMilli;
        diff = diff % minutesInMilli;

        long elapsedSeconds = diff / secondsInMilli;

        return formatString(elapsedHours) + ":" + formatString(elapsedMinutes) + ":" + formatString(elapsedSeconds);
    }

    public static String formatString(final float value) {
        return String.format(Locale.getDefault(), "%02d", (int) value);
    }

    private static String formatString(final long value) {
        return String.format(Locale.getDefault(), "%02d", (int) value);
    }

    public static String formatString(final int value) {
        return String.format(Locale.getDefault(), "%02d", value);
    }

    public static String getDate(long milliSeconds)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
