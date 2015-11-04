package com.innodroid.mongobrowser.util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.innodroid.mongobrowser.data.MongoCollectionRef;
import com.innodroid.mongobrowser.data.MongoDocument;
import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoNamespace;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.util.JSON;

import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoHelper {
	private static MongoClient Connection;
	private static MongoDatabase Database;
	private static String DatabaseName;
	private static String Server;
	private static int Port;
	private static String User;
	private static String Password;
	
	public static void connect(String server, int port, String dbname, String user, String pass) throws UnknownHostException {
		disconnect();

		ServerAddress sa = new ServerAddress(server, port);

		if (user != null && user.length() > 0) {
			List<MongoCredential> creds = new ArrayList<>();
			creds.add(MongoCredential.createScramSha1Credential(user, dbname, pass.toCharArray()));
			Connection = new MongoClient(sa, creds);
		} else {
			Connection = new MongoClient(sa);
		}

    	Database = Connection.getDatabase(dbname);
    	Server = server;
    	Port = port;
    	DatabaseName = dbname;
    	
    	User = user;
    	Password = pass;
    	
    	Connection.setWriteConcern(WriteConcern.SAFE);
		Database.listCollectionNames().first();
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

    public static void changeDatabase(String name) throws UnknownHostException {
    	Log.i("MONGO", "Change to database " + name);
		disconnect();
		connect(Server, Port, name, User, Password);
    }
    
	public static List<MongoCollectionRef> getCollectionNames(boolean includeSystemPrefs) {
		MongoIterable<String> names = Database.listCollectionNames();
    	ArrayList<MongoCollectionRef> list = new ArrayList<>();
    	
    	for (String str : names)
    		if (includeSystemPrefs || !str.startsWith("system."))
    			list.add(new MongoCollectionRef(str));

		return list;
    }

	public static ArrayList<String> getDatabaseNames() {
    	ArrayList<String> list = new ArrayList<String>();

    	for (String str : Connection.getDatabaseNames())
			list.add(str);

    	return list;
    }
	
	public static long getCollectionCount(String name) {
		return Database.getCollection(name).count();
	}
	
	public static void createCollection(String name) throws Exception {
		Database.createCollection(name);
	}
	
	public static void renameCollection(String oldName, String newName) throws UnknownHostException {
		reconnect();
		MongoNamespace ns = new MongoNamespace(Database.getName() + "." + newName);
		Database.getCollection(oldName).renameCollection(ns);
	}

	public static List<MongoDocument> getPageOfDocuments(String collection, String queryText, int start, int take) {
		MongoCollection coll = Database.getCollection(collection);
		FindIterable main = (queryText == null) ? coll.find() : coll.find(Document.parse(queryText));
		FindIterable items = main.skip(start).limit(take);
		final ArrayList<MongoDocument> results = new ArrayList<>();

		items.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				results.add(new MongoDocument(document.toJson()));
			}
		});

		return results;
	}

	public static void dropCollection(String name) {
		Database.getCollection(name).drop();
	}

	public static String saveDocument(String collectionName, String content) {
		Document doc = Document.parse(content);

		if (doc.containsKey("_id")) {
			Document filter = new Document("_id", doc.get("_id"));
			Database.getCollection(collectionName).findOneAndReplace(filter, doc);
		} else {
			Database.getCollection(collectionName).insertOne(doc);
		}

		return doc.toJson();
	}

	public static void deleteDocument(String collectionName, String content) {
		Document doc = Document.parse(content);

		if (doc.containsKey("_id")) {
			Document filter = new Document("_id", doc.get("_id"));
			Database.getCollection(collectionName).findOneAndDelete(filter);
		}
	}
}
