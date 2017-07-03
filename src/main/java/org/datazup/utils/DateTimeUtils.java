
package org.datazup.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import java.time.format.DateTimeFormatter;


/**
 * Created by ninel on 11/25/16.
 */

public class DateTimeUtils {

    private static List<DateTimeFormatter> COMMON_DATE_TIME_FORMATS =
            Arrays.asList(ISODateTimeFormat.dateTime(),
                    getFormatter("EEE MMM dd HH:mm:ss Z yyyy"),
                    getFormatter("EEE MMM dd HH:mm:ss z yyyy"));

    private static DateTimeFormatter getFormatter(String format) {
        /*DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern(format)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter(Locale.ENGLISH);*/

       DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withZoneUTC().withLocale(Locale.ENGLISH);
        return fmt;
    }

    public static Instant resolve(Object obj) {
        if (obj instanceof DateTime) {
            return from((DateTime)obj);
        } else if (obj instanceof Long)
            return Instant.ofEpochMilli((Long) obj);
        else if (obj instanceof String) {
            for (DateTimeFormatter fmt: COMMON_DATE_TIME_FORMATS){
                Instant dt = resolve(fmt, (String)obj);
                if (null!=dt){
                    return dt;
                }
            }
        }
        return null;
    }

    public static Instant from(DateTime dt){
        Instant i = Instant.ofEpochMilli(dt.getMillis());
        return i;
    }

    public static Instant resolve(DateTimeFormatter fmt, String dateString){
        /*try {
            TemporalAccessor temporalAccessor = fmt.parse(dateString);
            LocalDateTime localDateTime = LocalDateTime.from(temporalAccessor);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
            Instant dt = Instant.from(zonedDateTime);
            return dt;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }*/

        try {
            DateTime dt = fmt.parseDateTime(dateString);
            return resolve(dt);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static Instant resolve(Long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    public static Instant resolve(String datetime) {
        return Instant.parse(datetime);
    }

    public static Instant resolve(String datetime, String format) {
        DateTimeFormatter fmt = getFormatter(format);
        return resolve(fmt, datetime);
    }

    public static int getSecond(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getSecond();
    }
    public static int getMinute(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getMinute();
    }
    public static int getHour(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getHour();
    }
    public static int getDayOfMonth(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getDayOfMonth();
    }

    public static int getMonth(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getMonthValue();
    }
    public static int getYear(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).getYear();
    }

    public static Instant format(Instant dt, String format) {
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withLocale(Locale.ENGLISH).withZone(ZoneOffset.UTC);
        DateTimeFormatter formatter = getFormatter(format);
        DateTime dti = new DateTime( dt.toEpochMilli());
        String dtString = dti.toString(formatter);
        return resolve(dtString, format);
    }
    public static Instant format(DateTime dt, String format) {
        String dtString = dt.toString(format);
        return resolve(dtString, format);
    }

    public static Instant format(Date dt, String format) {
        Instant instant = dt.toInstant();
        return format(instant, format);
    }

    public static Instant format(Long dt, String format) {
        Instant instant = Instant.ofEpochMilli(dt);
        return format(instant, format);
    }

}
