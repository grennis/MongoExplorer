package com.innodroid.mongobrowser.util;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Patterns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map.Entry;
import java.util.regex.Matcher;

public class JsonUtils {
	private static final String TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
	private static final String TAG = "JsonUtils";

	public static String prettyPrint(String json) {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(json);
			return gson.toJson(je);
		} catch (Exception ex) {
			ex.printStackTrace();
			return json;
		}
	}

	public static Spanned prettyPrint2(String json) {
		try {
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(json);
			StringBuilder sb = new StringBuilder();

			Log.i(TAG, "prettyPrint2");
			printElement("", je, sb);

			return Html.fromHtml(sb.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			return Html.fromHtml(json);
		}
	}

	private static void printElement(String tab, JsonElement el,
			StringBuilder sb) {
		if (el.isJsonObject()) {
			JsonObject object = el.getAsJsonObject();
			if (isObjectId(object)) {
				sb.append("ObjectId("
						+ formatObjectString(object.get("$oid").getAsString()) + ")");
			} else if (isDate(object)) {
				sb.append("ISODate("
						+ formatObjectString(object.get("$date").getAsString()) + ")");
			} else {
				sb.append("{<br/>");
				String tab2 = tab + TAB;
				for (Entry<String, JsonElement> entry : object.entrySet()) {
					Log.i(TAG, entry.getKey());
					sb.append(tab2 + formatKey(entry.getKey()) + ": ");
					printElement(tab2, entry.getValue(), sb);
					sb.append(",<br/>");
				}
				sb.append(tab + "}");
			}
		} else if (el.isJsonArray()) {
			JsonArray array = el.getAsJsonArray();
			if (array.size() == 0) {
				sb.append("[ ]");
			} else {
				sb.append("[<br/>");
				String tab2 = tab + TAB;
				for (int i = 0; i < array.size(); i++) {
					sb.append(tab2);
					printElement(tab2, array.get(i), sb);
					if (i + 1 < array.size())
						sb.append(",<br/>");

				}
				sb.append("<br/>" + tab + "]");
			}
		} else if (el.isJsonNull()) {
			sb.append("null");
		} else {
			boolean handled = false;
			String string = el.getAsString();

			if ("true".equals(string) || "false".equals(string)) {
				handled = true;
				sb.append(formatBoolean(el.getAsString()));
			}

			try {
				Long.parseLong(string);
				sb.append(formatNumber(el.getAsString()));
				handled = true;
			} catch (NumberFormatException nfe) {
				// ignore errors
			}

			// fallback to string
			if (!handled) {
				sb.append(formatString(el.getAsString()));
			}
		}
	}

	private static String formatKey(String value) {
		return "<font color=\"#444444\">" + value + "</font>";
	}

	private static String formatNumber(String value) {
		return "<font color=\"#660000\">" + value + "</font>";
	}

	private static String formatBoolean(String value) {
		return "<font color=\"#660000\">" + value + "</font>";
	}

	private static String formatString(String value) {
		if (isUrl(value)) {
			value = Html.escapeHtml(value);
			value = "<a href=\"" + value + "\">" + value + "</a>";
		} else {
			value = Html.escapeHtml(value);
		}
		return "<font color=\"#006600\">\"" + value + "\"</font>";
	}

	private static String formatObjectString(String value) {
		return "<font color=\"#004488\">\"" + Html.escapeHtml(value)
				+ "\"</font>";
	}

	private static boolean isObjectId(JsonObject object) {
		if (object.entrySet().size() == 1) {
			if (object.has("$oid"))
				return true;
		}

		return false;
	}

	private static boolean isDate(JsonObject object) {
		if (object.entrySet().size() == 1) {
			if (object.has("$date"))
				return true;
		}
		return false;
	}

	private static boolean isUrl(String value) {
		Matcher matcher = Patterns.WEB_URL.matcher(value);
		return matcher.matches();
	}
}
