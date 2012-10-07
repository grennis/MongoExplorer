package com.innodroid.mongobrowser.data;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.innodroid.mongobrowser.R;

public class MongoCollectionAdapter extends ArrayAdapter<String> {
	public MongoCollectionAdapter(Context context) {
		super(context, R.layout.list_item_collection, R.id.list_item_collection_name);
	}

	public void loadItems(String[] items) {
		clear();
		
		for (String item : items) {
			add(item);
		}
	}
}
