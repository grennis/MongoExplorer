package com.innodroid.mongobrowser.data;

public class MongoCollectionRef {
	public String Name;
	public long Count;

	public MongoCollectionRef() {
	}

	public MongoCollectionRef(String name) {
		this.Name = name;
		this.Count = -1;
	}
}
