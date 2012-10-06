package com.innodroid.mongobrowser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.innodroid.mongobrowser.data.MongoBrowserProvider;
import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;

public class ConnectionSetupDialogFragment extends DialogFragment implements LoaderCallbacks<Cursor> {
	private static String ARG_CONNECTION_ID = "connid";
	
	private long mID;
	private TextView mNameView;
	private TextView mServerView;
	private TextView mPortView;
	private TextView mDatabaseView;
	private TextView mUserView;
	private TextView mPasswordView;

    static ConnectionSetupDialogFragment create(long id) {
    	ConnectionSetupDialogFragment fragment = new ConnectionSetupDialogFragment();
    	Bundle args = new Bundle();
    	args.putLong(ARG_CONNECTION_ID, id);
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_setup_connection, null);

    	mID = getArguments().getLong(ARG_CONNECTION_ID, 0);
    	mNameView = (TextView)view.findViewById(R.id.setup_connection_name);
    	mServerView = (TextView)view.findViewById(R.id.setup_connection_server);
    	mPortView = (TextView)view.findViewById(R.id.setup_connection_port);
    	mDatabaseView = (TextView)view.findViewById(R.id.setup_connection_db);
    	mUserView = (TextView)view.findViewById(R.id.setup_connection_user);
    	mPasswordView = (TextView)view.findViewById(R.id.setup_connection_pass);
    	
    	if (mID != 0)
    		getLoaderManager().initLoader(0, getArguments(), this);

        return new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_menu_edit)
                .setView(view)
                .setTitle(R.string.title_setup_connection)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	if (saveConnection())
                        		dialog.dismiss();
                        }
                    }
                )
                .setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	//
                        }
                    }
                )
                .create();
    }

    private boolean saveConnection() {    	
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

    	if (mID == 0)
    		helper.addConnection(name, server, port, db, user, pass);
    	else
    		helper.updateConnection(mID, name, server, port, db, user, pass);
    	
    	Intent intent = new Intent(Constants.MessageRefreshConnectionList);
    	LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    	return true;
    }
    
	public Loader<Cursor> onCreateLoader(int arg0, Bundle args) {
		Uri uri = ContentUris.withAppendedId(MongoBrowserProvider.CONNECTION_URI, args.getLong(ARG_CONNECTION_ID));
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


