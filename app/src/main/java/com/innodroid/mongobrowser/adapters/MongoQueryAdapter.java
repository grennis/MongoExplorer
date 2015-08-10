package com.innodroid.mongobrowser.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.innodroid.mongobrowser.data.MongoBrowserProvider;

public class MongoQueryAdapter extends android.support.v4.widget.CursorAdapter {
	public MongoQueryAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {		
		((TextView)view.findViewById(android.R.id.text1)).setText(cursor.getString(MongoBrowserProvider.INDEX_QUERY_NAME));
	}

	@Override
	public View newView(Context context, Cursor cursor, final ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        return inflater.inflate(android.R.layout.simple_list_item_1, null);
	}
}
