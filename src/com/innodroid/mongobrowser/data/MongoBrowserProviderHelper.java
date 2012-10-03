package com.innodroid.mongobrowser.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.BaseColumns;
import android.util.Log;

public class MongoBrowserProviderHelper {
	private static final String LOG_TAG = "MongoBrowserProviderHelper";
	private ContentResolver mResolver;

	public MongoBrowserProviderHelper(ContentResolver resolver) {
		mResolver = resolver;
	}

	public void addConnection(String name, String server, int port, String db, String user, String pass) {
		Log.i(LOG_TAG, "Adding Connection");
		
		ContentValues values = getContentValuesForConnection(name, server, port, db, user, pass);
		mResolver.insert(MongoBrowserProvider.CONNECTION_URI, values);
	}
	
	public void updateConnection(long id, String name, String server, int port, String db, String user, String pass) {
		Log.i(LOG_TAG, "Updating Connection");

		ContentValues values = getContentValuesForConnection(name, server, port, db, user, pass);
		mResolver.update(MongoBrowserProvider.CONNECTION_URI, values, BaseColumns._ID + " = ?", new String[] { Long.toString(id) });
	}

	public void deleteConnection(long id) {
		Log.i(LOG_TAG, "Deleting Connection");

		mResolver.delete(MongoBrowserProvider.CONNECTION_URI, BaseColumns._ID + " = ?", new String[] { Long.toString(id) });
	}

	public int deleteAllConnections() {
		Log.i(LOG_TAG, "Deleting all Connections");
		return mResolver.delete(MongoBrowserProvider.CONNECTION_URI, null, null);
	}

	private ContentValues getContentValuesForConnection(String name, String server, int port, String db, String user, String pass) {
		ContentValues cv = new ContentValues();
		cv.put(MongoBrowserProvider.NAME_CONNECTION_NAME, name);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_SERVER, server);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_PORT, port);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_DB, db);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_USER, user);
		cv.put(MongoBrowserProvider.NAME_CONNECTION_PASSWORD, pass);
		return cv;
	}
}

