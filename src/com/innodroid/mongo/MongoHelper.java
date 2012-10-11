package com.innodroid.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

public class MongoHelper {
	public static Mongo Connection;
	public static DB Database;
	
	public static void connect(String server, int port, String dbname, String user, String pass) throws UnknownHostException {
		disconnect();
		
		Connection = new Mongo(server, port);
    	Database = Connection.getDB(dbname);
    	
    	if (user != null && user.length() > 0)
    		Database.authenticate(user, pass.toCharArray());		
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

	public static long getCollectionCount(String name) {
		return Database.getCollection(name).getCount();
	}
	
	public static void renameCollection(String oldName, String newName) {
		Database.getCollection(oldName).rename(newName, false);
	}

	public static String[] getPageOfDocuments(String collection, int start, int take) {
		DBCollection coll = Database.getCollection(collection);
		DBCursor cursor = coll.find().skip(start).limit(take);		
		ArrayList<String> results = new ArrayList<String>();
		
		while (cursor.hasNext()) {
			results.add(cursor.curr().toString());
		}
		
		String[] res = new String[results.size()];
		results.toArray(res);
		return res;
	}
}
