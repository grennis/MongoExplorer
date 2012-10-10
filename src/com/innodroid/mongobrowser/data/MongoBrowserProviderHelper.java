package com.innodroid.mongobrowser.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class MongoBrowserProviderHelper {
	private static final String LOG_TAG = "MongoBrowserProviderHelper";
	private ContentResolver mResolver;

	public MongoBrowserProviderHelper(ContentResolver resolver) {
		mResolver = resolver;
	}

	public long addConnection(String name, String server, int port, String db, String user, String pass) {
		Log.i(LOG_TAG, "Adding Connection");

		ContentValues values = getContentValuesForConnection(name, server, port, db, user, pass);
		Uri uri = mResolver.insert(MongoBrowserProvider.CONNECTION_URI, values);
		return ContentUris.parseId(uri);
	}
	
	public void updateConnection(long id, String name, String server, int port, String db, String user, String pass) {
		Log.i(LOG_TAG, "Updating Connection");

		ContentValues values = getContentValuesForConnection(name, server, port, db, user, pass);
		mResolver.update(MongoBrowserProvider.CONNECTION_URI, values, BaseColumns._ID + " = ?", new String[] { Long.toString(id) });
	}

	public void updateConnectionLastConnect(long id) {
		Log.i(LOG_TAG, "Updating Connection");
		long lastConnect = System.currentTimeMillis();

		ContentValues cv = new ContentValues();
		cv.put(MongoBrowserProvider.NAME_CONNECTION_LAST_CONNECT, lastConnect);
		mResolver.update(MongoBrowserProvider.CONNECTION_URI, cv, BaseColumns._ID + " = ?", new String[] { Long.toString(id) });
	}

	public void deleteConnection(long id) {
		Log.i(LOG_TAG, "Deleting Connection");

		mResolver.delete(MongoBrowserProvider.CONNECTION_URI, BaseColumns._ID + " = ?", new String[] { Long.toString(id) });
	}

	public int deleteAllConnections() {
		Log.i(LOG_TAG, "Deleting all Connections");
		return mResolver.delete(MongoBrowserProvider.CONNECTION_URI, null, null);
	}

	public static ContentValues getContentValuesForConnection(String name, String server, int port, String db, String user, String pass) {
		ContentValues cv = new ContentValues();
		cv.put(MongoBrowserProvider.NAME_CONNECTION_NAME, name);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_SERVER, server);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_PORT, port);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_DB, db);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_USER, user);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_PASSWORD, pass);
		return cv;
	}

	public int getConnectionCount() {
		Cursor cursor = mResolver.query(MongoBrowserProvider.CONNECTION_URI, null, null, null, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}
}

