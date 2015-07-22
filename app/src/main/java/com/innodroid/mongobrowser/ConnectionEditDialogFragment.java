package com.innodroid.mongobrowser;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.innodroid.mongobrowser.data.MongoBrowserProvider;
import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;
import com.innodroid.mongobrowser.util.UiUtils;

public class ConnectionEditDialogFragment extends DialogFragment implements LoaderCallbacks<Cursor> {
	private TextView mNameView;
	private TextView mServerView;
	private TextView mPortView;
	private TextView mDatabaseView;
	private TextView mUserView;
	private TextView mPasswordView;
	private Callbacks mCallbacks;

	public interface Callbacks {
		void onConnectionAdded(long id);
		void onConnectionUpdated(long id);
	}
	
	public ConnectionEditDialogFragment() {
		super();
	}
	
    static ConnectionEditDialogFragment create(long id) {
    	ConnectionEditDialogFragment fragment = new ConnectionEditDialogFragment();
    	Bundle args = new Bundle();
    	args.putLong(Constants.ARG_CONNECTION_ID, id);
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_connection_edit, null);

    	long id = getArguments().getLong(Constants.ARG_CONNECTION_ID, 0);
    	mNameView = (TextView)view.findViewById(R.id.edit_connection_name);
    	mServerView = (TextView)view.findViewById(R.id.edit_connection_server);
    	mPortView = (TextView)view.findViewById(R.id.edit_connection_port);
    	mDatabaseView = (TextView)view.findViewById(R.id.edit_connection_db);
    	mUserView = (TextView)view.findViewById(R.id.edit_connection_user);
    	mPasswordView = (TextView)view.findViewById(R.id.edit_connection_pass);
    	
    	if (id != 0)
    		getLoaderManager().initLoader(0, getArguments(), this);

    	return UiUtils.buildAlertDialog(view, R.drawable.ic_mode_edit_black, R.string.title_edit_connection, true, 0, new UiUtils.AlertDialogCallbacks() {
			@Override
			public boolean onOK() {
				return save();
			}

			@Override
			public boolean onNeutralButton() {
				return false;
			}
		});    	
    }

    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);

    	if (getTargetFragment() != null)
    		mCallbacks = (Callbacks)getTargetFragment();
    	else
    		mCallbacks = (Callbacks)activity;
    }
    
    private boolean save() {    	
    	String name = mNameView.getText().toString();
    	String server = mServerView.getText().toString();
    	String porttxt = mPortView.getText().toString();
    	String db = mDatabaseView.getText().toString();
    	String user = mUserView.getText().toString();
    	String pass = mPasswordView.getText().toString();

    	if (name.length() == 0 || server.length() == 0 || porttxt.length() == 0 || db.length() == 0) {
    		Toast.makeText(getActivity(), "Required values not provided", Toast.LENGTH_SHORT).show();
    		return false;
    	}
   
    	int port = 0;
    	try {
    		port = Integer.parseInt(porttxt);
    	} catch (Exception e) {
    		Toast.makeText(getActivity(), "Port must be a number", Toast.LENGTH_SHORT).show();
    		return false;
    	}

    	MongoBrowserProviderHelper helper = new MongoBrowserProviderHelper(getActivity().getContentResolver());

    	long id = getArguments().getLong(Constants.ARG_CONNECTION_ID, 0);

    	if (id == 0) {
    		id = helper.addConnection(name, server, port, db, user, pass);
    		mCallbacks.onConnectionAdded(id);
    	}
    	else {
    		helper.updateConnection(id, name, server, port, db, user, pass);
        	mCallbacks.onConnectionUpdated(id);
    	}
    	
    	return true;
    }
    
	public Loader<Cursor> onCreateLoader(int arg0, Bundle args) {
		Uri uri = ContentUris.withAppendedId(MongoBrowserProvider.CONNECTION_URI, args.getLong(Constants.ARG_CONNECTION_ID));
	    return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (!cursor.moveToFirst())
			return;
		
    	mNameView.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_NAME));
    	mServerView.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_SERVER));
    	mPortView.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_PORT));
    	mDatabaseView.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_DB));
    	mUserView.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_USER));
    	mPasswordView.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_PASSWORD));    	
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
	}
}


