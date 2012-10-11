package com.innodroid.mongobrowser.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.innodroid.mongobrowser.R;

public class MongoCollectionAdapter extends ArrayAdapter<NameAndCount> {
	
	public MongoCollectionAdapter(Context context) {
		super(context, R.layout.list_item_collection, R.id.list_item_collection_name);
	}

	public void loadItems(String[] names) {
		clear();
		
		for (String name : names) {
			NameAndCount item = new NameAndCount();
			item.Name = name;
			item.Count = -1;
			add(item);
		}
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = inflater.inflate(R.layout.list_item_collection, null);
			
			ViewHolder holder = new ViewHolder();
			holder.NameView = (TextView)view.findViewById(R.id.list_item_collection_name);
			holder.CountView = (TextView)view.findViewById(R.id.list_item_collection_count);
			view.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder)view.getTag();
		NameAndCount item = getItem(position);
		holder.NameView.setText(item.Name);

		if (item.Count >= 0)
			holder.CountView.setText(Long.toString(item.Count));			
		else
			holder.CountView.setText("");
		
		return view;
	}
	
	public String getCollectionName(int position) {
		return getItem(position).Name;
	}
	
	public void setItemCount(int position, long count) {
		NameAndCount item = getItem(position);
		item.Count = count;
		notifyDataSetChanged();
	}
	
	public void add(String name) {
		NameAndCount item = new NameAndCount();
		item.Name = name;
		item.Count = 0;
		add(item);
		notifyDataSetChanged();		
	}
	
	private class ViewHolder 
	{
		public TextView NameView;
		public TextView CountView;
	}
}
