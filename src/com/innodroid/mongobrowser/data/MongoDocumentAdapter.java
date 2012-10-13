package com.innodroid.mongobrowser.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.innodroid.mongobrowser.R;

public class MongoDocumentAdapter extends ArrayAdapter<String> {
	
	public MongoDocumentAdapter(Context context) {
		super(context, R.layout.list_item_document, R.id.list_item_document_text);
	}

	public void loadItems(String[] names) {
		clear();
		
		for (String name : names) {
			add(name);
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		return (position == getCount()-1) ? 0 : 1;
	}
	
	@Override
	public int getCount() {
		int count = super.getCount();
		return (count == 0) ? 0 : count+1;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (position == getCount()-1)
			return getLoadMoreView(position, view, parent);
		else
			return getDocumentItemView(position, view, parent);
	}
	
	private View getLoadMoreView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = inflater.inflate(R.layout.list_item_load_more, null);		
		}
		
		return view;
	}
	
	private View getDocumentItemView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = inflater.inflate(R.layout.list_item_document, null);
			
			ViewHolder holder = new ViewHolder();
			holder.NameView = (TextView)view.findViewById(R.id.list_item_document_text);
			view.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder)view.getTag();
		String item = getItem(position);
		holder.NameView.setText(item);

		return view;
	}
	
	private class ViewHolder {
		public TextView NameView;
	}
}
