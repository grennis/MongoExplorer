package com.innodroid.mongobrowser.data;

public class MongoCollection {
	public String Name;
	public long Count;

	public MongoCollection() {
	}

	public MongoCollection(String name) {
		this.Name = name;
		this.Count = -1;
	}
}
