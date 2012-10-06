package com.innodroid.mongobrowser;

import com.innodroid.mongobrowser.data.MongoBrowserProvider;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConnectionDetailFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private TextView mTitle;
	private TextView mInfo;
    public static final String ARG_CONNECTION_ID = "item_id";

    public ConnectionDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_connection_detail, container, false);
        mTitle = (TextView) rootView.findViewById(R.id.connection_detail_title);
        mInfo = (TextView) rootView.findViewById(R.id.connection_detail_info);
        
        getLoaderManager().initLoader(0, getArguments(), this);
        
        return rootView;
    }
    
	public Loader<Cursor> onCreateLoader(int arg0, Bundle args) {
		Uri uri = ContentUris.withAppendedId(MongoBrowserProvider.CONNECTION_URI, args.getLong(ARG_CONNECTION_ID));
	    return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (!cursor.moveToFirst())
			return;
		
		mTitle.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_NAME));
		mInfo.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_DB) + " on " + cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_SERVER));

		getActivity().setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
	}
}
