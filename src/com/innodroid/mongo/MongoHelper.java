package com.innodroid.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import android.util.Log;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

public class MongoHelper {
	private static Mongo Connection;
	private static DB Database;
	private static DB LoginDatabase;
	private static String DatabaseName;
	private static String Server;
	private static int Port;
	private static String User;
	private static String Password;
	
	public static void connect(String server, int port, String dbname, String user, String pass) throws UnknownHostException {
		disconnect();
		
		Connection = new Mongo(server, port);
    	Database = Connection.getDB(dbname);
    	LoginDatabase = Database;
    	Server = server;
    	Port = port;
    	DatabaseName = dbname;
    	
    	User = user;
    	Password = pass;
    	
    	if (user != null && user.length() > 0) {
    		Database.authenticate(user, pass.toCharArray());
    	} 
    	
    	Connection.setWriteConcern(WriteConcern.SAFE);
	}
	
    private static void disconnect() {
    	try {
	    	if (Connection != null) {
	    		Connection.close();
	    		Connection = null;
	    		Database = null;
	    	}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
	}
    
    private static void reconnect() throws UnknownHostException {
    	disconnect();
    	connect(Server, Port, DatabaseName, User, Password);
    }

    public static void changeDatabase(String name) {
    	Log.i("MONGO", "CHange to database " + name);
    	Database = LoginDatabase.getSisterDB(name);
    }
    
	public static String[] getCollectionNames(boolean includeSystemPrefs) {
    	Set<String> names = Database.getCollectionNames();
    	ArrayList<String> list = new ArrayList<String>();
    	
    	for (String str : names)
    		if (includeSystemPrefs || !str.startsWith("system."))
    			list.add(str);

    	String[] namesArray = new String[list.size()];
    	list.toArray(namesArray);
    	return namesArray;
    }

	public static ArrayList<String> getDatabaseNames() {
    	ArrayList<String> list = new ArrayList<String>();

    	for (String str : Connection.getDatabaseNames())
			list.add(str);

    	return list;
    }
	
	public static long getCollectionCount(String name) {
		return Database.getCollection(name).getCount();
	}
	
	public static void createCollection(String name) throws Exception {
		if (Database.collectionExists(name))
			throw new Exception ("Collection already exists");
		DBObject obj = new BasicDBObject();
		obj.put("_id", "1");
		Database.getCollection(name).save(obj);
		Database.getCollection(name).remove(obj);
	}
	
	public static void renameCollection(String oldName, String newName) throws UnknownHostException {
		reconnect();
		Database.getCollection(oldName).rename(newName);
	}

	public static String[] getPageOfDocuments(String collection, String queryText, int start, int take) {
		DBCollection coll = Database.getCollection(collection);
		DBCursor main = (queryText == null) ? coll.find() : coll.find(parse(queryText));
		DBCursor cursor = main.skip(start).limit(take);
		
		ArrayList<String> results = new ArrayList<String>();
		
		while (cursor.hasNext()) {
			cursor.next();
			results.add(cursor.curr().toString());
		}
		
		cursor.close();
		main.close();
		String[] res = new String[results.size()];
		results.toArray(res);
		return res;
	}

	public static void dropCollection(String name) {
		Database.getCollection(name).drop();
	}

	public static String saveDocument(String collectionName, String content) {
		DBObject obj = parse(content);
		Database.getCollection(collectionName).save(obj);
		return obj.toString();
	}

	public static void deleteDocument(String collectionName, String content) {
		Database.getCollection(collectionName).remove(parse(content));
	}
	
	private static DBObject parse(String text) {
		Object obj = com.mongodb.util.JSON.parse(text);
		return (DBObject)obj;
	}
}
