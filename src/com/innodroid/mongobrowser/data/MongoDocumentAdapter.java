package com.innodroid.mongobrowser.data;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.innodroid.mongobrowser.R;

public class MongoDocumentAdapter extends BaseAdapter {
	private Context mContext;
	private boolean mShowLoadMore;
	private ArrayList<String> mItems = new ArrayList<String>();
	
	public MongoDocumentAdapter(Context context) {
		mContext = context;
		mShowLoadMore = true;
	}

	public void addAll(String[] names) {
		for (String name : names) {
			mItems.add(name);
		}
		
		notifyDataSetChanged();
	}
	
	@Override
	public int getItemViewType(int position) {
		if (!mShowLoadMore)
			return 1;
		
		return (position == getCount()-1) ? 0 : 1;
	}
	
	@Override
	public int getCount() {
		int count = mItems.size();
		
		if (!mShowLoadMore)
			return count;
		
		return (count == 0) ? 0 : count+1;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (mShowLoadMore && position == getCount()-1)
			return getLoadMoreView(position, view, parent);
		else
			return getDocumentItemView(position, view, parent);
	}
	
	private View getLoadMoreView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = inflater.inflate(R.layout.list_item_load_more, null);		
		}
		
		return view;
	}
	
	private View getDocumentItemView(int position, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			view = inflater.inflate(R.layout.list_item_document, null);
			
			ViewHolder holder = new ViewHolder();
			holder.NameView = (TextView)view.findViewById(R.id.list_item_document_text);
			view.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder)view.getTag();
		String item = mItems.get(position);
		holder.NameView.setText(item);

		return view;
	}
	
	public void insertOrUpdate(int position, String content) {
		if (position < 0)
			mItems.add(0, content);
		else
			mItems.set(position, content);

		notifyDataSetChanged();
	}
	
	public void delete(int position) {
		mItems.remove(position);
		
		notifyDataSetChanged();
	}
	
	public void showLoadMore(boolean value) {
		mShowLoadMore = value;
		notifyDataSetChanged();
	}
	
	public boolean isShowingLoadMore() {
		return mShowLoadMore;
	}
	
	private class ViewHolder {
		public TextView NameView;
	}

	@Override
	public String getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
