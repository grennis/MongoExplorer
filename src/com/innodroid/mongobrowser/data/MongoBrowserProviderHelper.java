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

	public long saveQuery(long id, String name, long connectionId, String collectionName, String text) {
		Log.i(LOG_TAG, "Saving query");

		ContentValues cv = new ContentValues();
		cv.put(MongoBrowserProvider.NAME_QUERY_TEXT, text);
		cv.put(MongoBrowserProvider.NAME_QUERY_NAME, name);
		cv.put(MongoBrowserProvider.NAME_QUERY_CONN_ID, connectionId);
		cv.put(MongoBrowserProvider.NAME_QUERY_COLL_NAME, collectionName);

		if (id > 0) {
			Uri uri = ContentUris.withAppendedId(MongoBrowserProvider.QUERY_URI, id);
			mResolver.update(uri, cv, null, null);
			return id;
		} else {
			Uri result = mResolver.insert(MongoBrowserProvider.QUERY_URI, cv);			
			return Long.parseLong(result.getLastPathSegment());
		}
	}

	public void deleteConnection(long id) {
		Log.i(LOG_TAG, "Deleting Connection");

		mResolver.delete(MongoBrowserProvider.CONNECTION_URI, BaseColumns._ID + " = ?", new String[] { Long.toString(id) });
	}

	public int deleteAllConnections() {
		Log.i(LOG_TAG, "Deleting all Connections");
		return mResolver.delete(MongoBrowserProvider.CONNECTION_URI, null, null);
	}
	
	public Cursor findQuery(String name, long connectionId, String collectionName) {
		return mResolver.query(MongoBrowserProvider.QUERY_URI, null, MongoBrowserProvider.NAME_QUERY_NAME + " = ? and " + MongoBrowserProvider.NAME_QUERY_CONN_ID + " = ? and " + MongoBrowserProvider.NAME_QUERY_COLL_NAME + " = ?", new String[] { name, Long.toString(connectionId), collectionName }, null);
	}
	
	public Cursor getNamedQueries(long connectionId, String collectionName) {
		return mResolver.query(MongoBrowserProvider.QUERY_URI, null, MongoBrowserProvider.NAME_QUERY_CONN_ID + " = ? and " + MongoBrowserProvider.NAME_QUERY_COLL_NAME + " = ?", new String[] { Long.toString(connectionId), collectionName }, null);
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

