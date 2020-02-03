package uk.co.cardiff.council.morebike.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.room.TypeConverter;

public class DateTypeConverter {

    @TypeConverter
    public static String toString(Date value) {
        return value == null ? null : new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.UK).format(value);
    }

    @TypeConverter
    public static Date toDate(String value) {
        try {
            return value == null ? null : new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.UK).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
}
