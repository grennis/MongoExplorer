package com.innodroid.mongobrowser.adapters;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.data.MongoBrowserProvider;

public class MongoConnectionAdapter extends android.support.v4.widget.CursorAdapter {
	private Context mContext;
	
	public MongoConnectionAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		
		mContext = context;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder)view.getTag();
		holder.Id = cursor.getLong(MongoBrowserProvider.INDEX_CONNECTION_ID);
		holder.NameView.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_NAME));
		holder.InfoView.setText(getConnectionInfo(cursor));
		holder.StarCheckbox.setChecked(cursor.getLong(MongoBrowserProvider.INDEX_CONNECTION_FLAGS) != 0);
	}

	private CharSequence getConnectionInfo(Cursor cursor) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_DB));
		sb.append(" on ");
		sb.append(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_SERVER));
		
		return sb.toString();
	}

	@Override
	public View newView(Context context, Cursor cursor, final ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();

        View view = inflater.inflate(R.layout.list_item_connection, null);
        
        ViewHolder holder = new ViewHolder();
        holder.StarCheckbox = (CheckBox)view.findViewById(R.id.list_item_connection_checked);
        holder.NameView = (TextView)view.findViewById(R.id.list_item_connection_name);
        holder.InfoView = (TextView)view.findViewById(R.id.list_item_connection_info);
        view.setTag(holder);
        holder.StarCheckbox.setTag(holder);

        holder.StarCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				CheckBox btn = (CheckBox)arg0;
				ViewHolder holder = (ViewHolder)btn.getTag();
				new SetFlagsTask(holder.Id, btn.isChecked() ? 1 : 0).execute((Void)null);
			}
        });
        
        return view;
	}

	private class ViewHolder {
		public long Id;
		public CheckBox StarCheckbox;
		public TextView NameView;
		public TextView InfoView;
	}	

    private class SetFlagsTask extends AsyncTask<Void, Void, Boolean> {
    	private long mID;
    	private long mFlags;
    	
    	public SetFlagsTask(long id, long flags) {
    		mID = id;
    		mFlags = flags;
    	}
    	
		@Override
		protected Boolean doInBackground(Void... arg0) {
			ContentValues cv = new ContentValues();
			cv.put(MongoBrowserProvider.NAME_CONNECTION_FLAGS, mFlags);
			Uri uri = ContentUris.withAppendedId(MongoBrowserProvider.CONNECTION_URI, mID);
			mContext.getContentResolver().update(uri, cv, null, null);
			return true;
		}		
    }
}
