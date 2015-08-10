package com.innodroid.mongobrowser.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.data.MongoCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClientOptions;

import java.util.ArrayList;
import java.util.List;

public class MongoCollectionAdapter extends BaseAdapter {
	private Context mContext;
	private List<MongoCollection> mItems = new ArrayList<>();

	public MongoCollectionAdapter(Context context) {
		mContext = context;
	}

	public void setItems(List<MongoCollection> items) {
		mItems = items;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	public MongoCollection getCollection(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			view = inflater.inflate(R.layout.list_item_collection, null);
			
			ViewHolder holder = new ViewHolder();
			holder.NameView = (TextView)view.findViewById(R.id.list_item_collection_name);
			holder.CountView = (TextView)view.findViewById(R.id.list_item_collection_count);
			view.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder)view.getTag();
		MongoCollection item = mItems.get(position);
		holder.NameView.setText(item.Name);

		if (item.Count >= 0)
			holder.CountView.setText(Long.toString(item.Count));			
		else
			holder.CountView.setText("");
		
		return view;
	}
	
	public String getCollectionName(int position) {
		return mItems.get(position).Name;
	}

	public void setItemName(int position, String name) {
		MongoCollection item = mItems.get(position);
		item.Name = name;
		notifyDataSetChanged();
	}
	
	public void add(int position, String name) {
		MongoCollection item = new MongoCollection();
		item.Name = name;
		item.Count = 0;
		mItems.add(position, item);
		notifyDataSetChanged();		
	}
	
	public void delete(int position) {
		mItems.remove(position);
		notifyDataSetChanged();
	}

	public void notifyCountChanged(int position) {
		notifyDataSetChanged();
	}

	private class ViewHolder 
	{
		public TextView NameView;
		public TextView CountView;
	}
}
