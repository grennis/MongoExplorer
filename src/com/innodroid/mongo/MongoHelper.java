package com.innodroid.mongo;

import java.net.UnknownHostException;
import java.util.Set;

import com.mongodb.DB;
import com.mongodb.Mongo;

import android.util.Log;

public class MongoHelper {
    public void trygomongo() throws UnknownHostException {
    	Mongo m = new Mongo("alex.mongohq.com", 10053);
    	DB db = m.getDB("getupandgreen");
    	db.authenticate("admin", new char[] { 'h', 'e', 'l', 'l', 'o', '1', '2', '3' });
    	Set<String> names = db.getCollectionNames();
    	
    	for (String name : names)
    	{
    		Log.i("COLLECTION_NAME", name);
    	}
    }
}
