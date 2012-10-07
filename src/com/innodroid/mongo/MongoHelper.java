package com.innodroid.mongo;

import java.net.UnknownHostException;
import java.util.Set;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongoHelper {
	public static Mongo Connection;
	public static DB Database;
	
	public static void connect(String server, int port, String dbname, String user, String pass) throws UnknownHostException {
		disconnect();
		
		Connection = new Mongo(server, port);
    	Database = Connection.getDB(dbname);
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

	public String[] getCollectionNames() {
    	Set<String> names = Database.getCollectionNames();
    	String[] namesArray = new String[names.size()];
    	names.toArray(namesArray);
    	return namesArray;
    }
}
