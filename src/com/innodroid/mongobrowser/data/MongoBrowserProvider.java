package com.innodroid.mongobrowser.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class MongoBrowserProvider extends ContentProvider {

	private static final String LOG_TAG = "MongoBrowserProvider";
	private static final String DATABASE_NAME = "mongobrowser.db";
	public static final String TABLE_NAME_CONNECTIONS = "connections";
	public static final String TABLE_NAME_QUERIES = "queries";
	private static final int DATABASE_VERSION = 20;

	public static final int INDEX_CONNECTION_ID = 0;
	public static final int INDEX_CONNECTION_NAME = 1;
	public static final int INDEX_CONNECTION_SERVER = 2;
    public static final int INDEX_CONNECTION_PORT = 3; 
    public static final int INDEX_CONNECTION_DB = 4; 
	public static final int INDEX_CONNECTION_USER = 5;
	public static final int INDEX_CONNECTION_PASSWORD = 6;
	public static final int INDEX_CONNECTION_FLAGS = 7;
	public static final int INDEX_CONNECTION_LAST_CONNECT = 8;
	
	public static final String NAME_CONNECTION_NAME = "name";
	public static final String NAME_CONNECTION_SERVER = "server";
    public static final String NAME_CONNECTION_PORT = "port"; 
    public static final String NAME_CONNECTION_DB = "dbname"; 
	public static final String NAME_CONNECTION_USER = "usernm";
	public static final String NAME_CONNECTION_PASSWORD = "pass";
	public static final String NAME_CONNECTION_FLAGS = "cflags";
	public static final String NAME_CONNECTION_LAST_CONNECT = "lastconn";

	public static final int INDEX_QUERY_ID = 0;
	public static final int INDEX_QUERY_NAME = 1;
	public static final int INDEX_QUERY_CONN_ID = 2;
	public static final int INDEX_QUERY_COLL_NAME = 3;
	public static final int INDEX_QUERY_TEXT = 4;
	
	public static final String NAME_QUERY_NAME = "qname";
	public static final String NAME_QUERY_CONN_ID = "connid";
	public static final String NAME_QUERY_COLL_NAME = "coll";
	public static final String NAME_QUERY_TEXT = "qtext";

	//setup authority for provider
	private static final String AUTHORITY = "com.innodroid.provider.mongobrowser";

	//URI's to consume this provider
	public static final Uri CONNECTION_URI = Uri.parse("content://" + AUTHORITY + "/connections");
	public static final String CONNECTION_ITEM_TYPE = "vnd.android.cursor.item/vnd.innodroid.connection";
    public static final String CONNECTION_LIST_TYPE = "vnd.android.cursor.dir/vnd.innodroid.connection";

	public static final Uri QUERY_URI = Uri.parse("content://" + AUTHORITY + "/queries");
	public static final String QUERY_ITEM_TYPE = "vnd.android.cursor.item/vnd.innodroid.query";
    public static final String QUERY_LIST_TYPE = "vnd.android.cursor.dir/vnd.innodroid.query";

    //Create the statics used to differentiate between the different URI requests
    private static final int CONNECTION_ONE = 1; 
    private static final int CONNECTION_ALL = 2;
    private static final int QUERY_ONE = 3; 
    private static final int QUERY_ALL = 4;

	//database members
	private SQLiteOpenHelper mOpenHelper;
    private SQLiteDatabase mDatabase;

	private static final UriMatcher URI_MATCHER;
	
    static 
	{
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

        URI_MATCHER.addURI(AUTHORITY, "connections/#", CONNECTION_ONE);
        URI_MATCHER.addURI(AUTHORITY, "connections", CONNECTION_ALL);
        URI_MATCHER.addURI(AUTHORITY, "queries/#", QUERY_ONE);
        URI_MATCHER.addURI(AUTHORITY, "queries", QUERY_ALL);
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
			case QUERY_ALL:
				return QUERY_LIST_TYPE;
			case QUERY_ONE:
				return QUERY_ITEM_TYPE;
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
				String xid = uri.getPathSegments().get(1);
				cursor = mDatabase.query(TABLE_NAME_CONNECTIONS, projection, BaseColumns._ID + " = ?", new String[] { xid }, null, null, sortOrder);
				break;
			case QUERY_ALL:
				cursor = mDatabase.query(TABLE_NAME_QUERIES, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			case QUERY_ONE:
				String yid = uri.getPathSegments().get(1);
				cursor = mDatabase.query(TABLE_NAME_QUERIES, projection, BaseColumns._ID + " = ?", new String[] { yid }, null, null, sortOrder);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI:" + uri);				
		}
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);					
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table;
		Uri newUri;
		
		switch (URI_MATCHER.match(uri)) {
			case CONNECTION_ALL:
				table = TABLE_NAME_CONNECTIONS;
				newUri = CONNECTION_URI;
				break;
			case QUERY_ALL:
				table = TABLE_NAME_QUERIES;
				newUri = QUERY_URI;
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);			
		}

		long id = mDatabase.insert(table, null, values);
		newUri = ContentUris.withAppendedId(newUri, id);
		getContext().getContentResolver().notifyChange(newUri, null);
		Log.d(LOG_TAG, "Insert " + id);

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
				String xid = uri.getPathSegments().get(1);
				count = mDatabase.update(TABLE_NAME_CONNECTIONS, values, BaseColumns._ID + " = ?", new String[] { xid });
				break;
			case QUERY_ALL:
				count = mDatabase.update(TABLE_NAME_QUERIES, values, selection, selectionArgs);
				break;
			case QUERY_ONE:
				String yid = uri.getPathSegments().get(1);
				count = mDatabase.update(TABLE_NAME_QUERIES, values, BaseColumns._ID + " = ?", new String[] { yid });
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
				String xid = uri.getPathSegments().get(1);
				count = mDatabase.delete(TABLE_NAME_CONNECTIONS, BaseColumns._ID + " = ?", new String[] { xid });
				break;
			case QUERY_ALL:
				count = mDatabase.delete(TABLE_NAME_QUERIES, where, whereArgs);
				break;
			case QUERY_ONE:
				String yid = uri.getPathSegments().get(1);
				count = mDatabase.delete(TABLE_NAME_QUERIES, BaseColumns._ID + " = ?", new String[] { yid });
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
			createConnectionsTable(db);
			createQueriesTable(db);

			/*
			insertConnection(db, "Lumenbox DEV", "dbh61.mongolab.com", 27617, "lumenbox_dev", "lumenuser", "hellolumen");
			insertConnection(db, "Lumenbox PROD", "ds031277.mongolab.com", 31277, "lumenbox", "user", "user");
			insertConnection(db, "imMeta DEV", "ds031637.mongolab.com", 31637, "immeta_dev", "imm3ta", "passw0rd");
			insertConnection(db, "imMeta PROD", "ds029817.mongolab.com", 29817, "immeta_prod", "pr0dm3ta", "passw0rd");
			insertConnection(db, "atWork DEV", "ds033487.mongolab.com", 33487, "atwork_dev", "atwork", "!hello1!");
			insertConnection(db, "guag", "alex.mongohq.com", 10053, "getupandgreen", "admin", "hello123");	
			*/	
			
			/* This used for screenshots */
			/*
			insertConnection(db, "Demo App QA", "demo.mongolab.com", 57323, "demo_qa", "app_login", "passw0rd");
			insertConnection(db, "Demo App PROD", "demo.mongolab.com", 33487, "atwork_dev", "atwork", "!hello1!");
			insertConnection(db, "Support Database", "alex.mongohq.com", 10007, "support", "app_login", "hello123");		
			 */			
		}

		private void createConnectionsTable(SQLiteDatabase db) {
			Log.w(LOG_TAG, "Creating a new table - " + TABLE_NAME_CONNECTIONS);
			db.execSQL(
				"CREATE TABLE "  + TABLE_NAME_CONNECTIONS + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY, " 
				 + NAME_CONNECTION_NAME + " TEXT, "
				 + NAME_CONNECTION_SERVER + " TEXT, "
				 + NAME_CONNECTION_PORT + " INTEGER, "
				 + NAME_CONNECTION_DB + " TEXT, "
				 + NAME_CONNECTION_USER + " TEXT, "
				 + NAME_CONNECTION_PASSWORD + " TEXT, "
				 + NAME_CONNECTION_FLAGS + " INTEGER, "
				 + NAME_CONNECTION_LAST_CONNECT + " INTEGER"
				 + " );"
			);			
		}

		private void createQueriesTable(SQLiteDatabase db) {
			Log.w(LOG_TAG, "Creating a new table - " + TABLE_NAME_QUERIES);
			db.execSQL(
				"CREATE TABLE "  + TABLE_NAME_QUERIES + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY, " 
				 + NAME_QUERY_NAME + " TEXT, "
				 + NAME_QUERY_CONN_ID + " INTEGER, "
				 + NAME_QUERY_COLL_NAME + " TEXT, "
				 + NAME_QUERY_TEXT + " TEXT"
				 + " );"
			);			
		}
		
/*
		private long insertConnection(SQLiteDatabase dbx, String name, String server, int port, String db, String user, String pass) {
			ContentValues cv = MongoBrowserProviderHelper.getContentValuesForConnection(name, server, port, db, user, pass);
			return dbx.insert(TABLE_NAME_CONNECTIONS, null, cv);
		}
*/
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)  {
			Log.w(LOG_TAG, "Upgrade database");

			if (oldVersion < 20) {
				createQueriesTable(db);
			}
		}
	}
}

