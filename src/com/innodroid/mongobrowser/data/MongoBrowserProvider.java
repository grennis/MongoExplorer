package com.innodroid.mongobrowser.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class MongoBrowserProvider extends ContentProvider {

	private static final String LOG_TAG = "MongoBrowserProvider";
	private static final String DATABASE_NAME = "mongobrowser.db";
	public static final String TABLE_NAME_CONNECTIONS = "connections";
	private static final int DATABASE_VERSION = 3;

	public static final int INDEX_CONNECTION_ID = 0;
	public static final int INDEX_CONNECTION_NAME = 1;
	public static final int INDEX_CONNECTION_SERVER = 2;
    public static final int INDEX_CONNECTION_PORT = 3; 
    public static final int INDEX_CONNECTION_DB = 4; 
	public static final int INDEX_CONNECTION_USER = 5;
	public static final int INDEX_CONNECTION_PASSWORD = 6;
	public static final int INDEX_CONNECTION_FLAGS = 7;
	
	public static final String NAME_CONNECTION_NAME = "name";
	public static final String NAME_CONNECTION_SERVER = "server";
    public static final String NAME_CONNECTION_PORT = "port"; 
    public static final String NAME_CONNECTION_DB = "dbname"; 
	public static final String NAME_CONNECTION_USER = "usernm";
	public static final String NAME_CONNECTION_PASSWORD = "pass";
	public static final String NAME_CONNECTION_FLAGS = "cflags";

	//setup authority for provider
	private static final String AUTHORITY = "com.innodroid.provider.mongobrowser";

	//URI's to consume this provider
	public static final Uri CONNECTION_URI = Uri.parse("content://" + AUTHORITY + "/connections");
	public static final String CONNECTION_ITEM_TYPE = "vnd.android.cursor.item/vnd.innodroid.connection";
    public static final String CONNECTION_LIST_TYPE = "vnd.android.cursor.dir/vnd.innodroid.connection";

    //Create the statics used to differentiate between the different URI requests
    private static final int CONNECTION_ONE = 1; 
    private static final int CONNECTION_ALL = 2;

	//database members
	private SQLiteOpenHelper mOpenHelper;
    private SQLiteDatabase mDatabase;

	private static final UriMatcher URI_MATCHER;
	
    static 
	{
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

        URI_MATCHER.addURI(AUTHORITY, "connections/#", CONNECTION_ONE);
        URI_MATCHER.addURI(AUTHORITY, "connections", CONNECTION_ALL);
    }

	@Override
	public boolean onCreate() {
		 mOpenHelper = new DatabaseHelper(getContext());
		 mDatabase = mOpenHelper.getWritableDatabase();
		 return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case CONNECTION_ALL:
				return CONNECTION_LIST_TYPE;
			case CONNECTION_ONE:
				return CONNECTION_ITEM_TYPE;
			default: 
				throw new IllegalArgumentException("Unsupported URI:" + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Cursor cursor;
		
		switch (URI_MATCHER.match(uri)) {
			case CONNECTION_ALL:
				cursor = mDatabase.query(TABLE_NAME_CONNECTIONS, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			case CONNECTION_ONE:
				String id = uri.getPathSegments().get(1);
				cursor = mDatabase.query(TABLE_NAME_CONNECTIONS, projection, BaseColumns._ID + " = ?", new String[] { id }, null, null, sortOrder);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI:" + uri);				
		}
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);					
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (URI_MATCHER.match(uri) != CONNECTION_ALL)
			throw new IllegalArgumentException("Unknown URI " + uri);

		long id = mDatabase.insert(TABLE_NAME_CONNECTIONS, null, values);

		if (id < 0)
			throw new SQLException("Failed to insert row into " + uri);

		Uri newUri = ContentUris.withAppendedId(CONNECTION_URI, id);
		getContext().getContentResolver().notifyChange(newUri, null);
		Log.d(LOG_TAG, "Added CONNECTION ID " + id);
			
		return newUri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		
		switch (URI_MATCHER.match(uri))
		{
			case CONNECTION_ALL:
				count = mDatabase.update(TABLE_NAME_CONNECTIONS, values, selection, selectionArgs);
				break;
			case CONNECTION_ONE:
				String id = uri.getPathSegments().get(1);
				count = mDatabase.update(TABLE_NAME_CONNECTIONS, values, BaseColumns._ID + " = ?", new String[] { id });
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI:" + uri);				
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count=0;

		switch (URI_MATCHER.match(uri))
		{
			case CONNECTION_ALL:
				count = mDatabase.delete(TABLE_NAME_CONNECTIONS, where, whereArgs);
				break;
			case CONNECTION_ONE:
				String id = uri.getPathSegments().get(1);
				count = mDatabase.delete(TABLE_NAME_CONNECTIONS, BaseColumns._ID + " = ?", new String[] { id });
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI:" + uri);				
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)  {
		   createDatabase(db);
		}

		private void createDatabase(SQLiteDatabase db) {
			Log.w(LOG_TAG, "Creating a new table - " + TABLE_NAME_CONNECTIONS);
			db.execSQL(
				"CREATE TABLE "  + TABLE_NAME_CONNECTIONS + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY, " 
				 + NAME_CONNECTION_NAME + " TEXT, "
				 + NAME_CONNECTION_SERVER + " TEXT, "
				 + NAME_CONNECTION_PORT + " INTEGER, "
				 + NAME_CONNECTION_DB + " TEXT, "
				 + NAME_CONNECTION_USER + " TEXT, "
				 + NAME_CONNECTION_PASSWORD + " TEXT, "
				 + NAME_CONNECTION_FLAGS + " INTEGER"
				 + " );"
			);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)  {
			Log.w(LOG_TAG, "Upgrade by recreate");

			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CONNECTIONS);
			onCreate(db);
		}
	}
}

