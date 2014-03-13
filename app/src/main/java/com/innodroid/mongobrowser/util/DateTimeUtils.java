package com.innodroid.mongobrowser.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.TimeZone;

import android.content.Context;
import android.text.format.DateUtils;

public class DateTimeUtils {
	private static long ONE_DAY = 1 * 24 * 60 * 60 * 1000;
	private static long SIX_DAYS = 6 * 24 * 60 * 60 * 1000;
    private static Formatter mFormatter;
    private static StringBuilder mStringBuilder;

	public static long truncateTime(long value) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(value);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		return cal.getTimeInMillis();
	}
	
	public static String formatRelativeDate(Context context, long value) {
		long now = truncateTime(new Date().getTime());

		if (value == now)
			return "Today";
		else if (value == now + ONE_DAY)
			return "Tomorrow";
		else if (value == now - ONE_DAY)
			return "Yesterday";
		else if (value <= now + SIX_DAYS)
			return formatDateWeekday(context, value);
		
		return formatDate(context, value);
	}

	public static String formatDateWeekday(Context context, long value) {
		mStringBuilder.setLength(0);
		DateUtils.formatDateRange(context, mFormatter, value, value, DateUtils.FORMAT_SHOW_WEEKDAY, "GMT");
		return mStringBuilder.toString();
	}

	public static String formatDate(Context context, long value) {
		mStringBuilder.setLength(0);
		DateUtils.formatDateRange(context, mFormatter, value, value, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY, "GMT");
		return mStringBuilder.toString();
	}

	public static String formatLength(Context context, long length) {
		if (length == 0)
			return "";		
		else if (length < 60)
			return length + " Minutes";
		else if (length == 60)
			return "1 Hour";
		else if (length % 60 == 0)
			return (length / 60) + " Hours";

		long hour = length / 60;
		long mins = length % 60;
		
		return hour + " Hours " + mins + " Minutes";
	}
	
	public static String formatText(String text) {
		if (text == null)
			return "";
		
		return text;
	}
	
	public static long parseIsoDate(String value) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.parse(value).getTime();
	}
}
