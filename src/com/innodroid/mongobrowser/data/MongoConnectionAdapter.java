package com.innodroid.mongobrowser.data;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.innodroid.mongobrowser.R;

public class MongoConnectionAdapter extends android.support.v4.widget.CursorAdapter {
	@SuppressWarnings("unused")
	private Context mContext;
	
	public MongoConnectionAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		
		mContext = context;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder)view.getTag();
		//.Id = cursor.getLong(MongoBrowserProvider.INDEX_CONNECTION_ID);
		holder.NameView.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_NAME));
		holder.UrlView.setText(getConnectionUrlText(cursor));
	}

	private CharSequence getConnectionUrlText(Cursor cursor) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("mongo://");
		sb.append(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_SERVER));
		sb.append(":");
		sb.append(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_PORT));
		sb.append("/");
		sb.append(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_DB));
		
		return sb.toString();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();

        View view = inflater.inflate(R.layout.list_item_connection, null);
        
        ViewHolder holder = new ViewHolder();
        holder.NameView = (TextView)view.findViewById(R.id.list_item_connection_name);
        holder.UrlView = (TextView)view.findViewById(R.id.list_item_connection_url);
        view.setTag(holder);
                
        return view;
	}

	private class ViewHolder {
		//public long Id;
		public TextView NameView;
		public TextView UrlView;
	}	
}
