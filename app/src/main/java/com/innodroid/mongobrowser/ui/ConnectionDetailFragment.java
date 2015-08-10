package com.innodroid.mongobrowser.ui;

import java.net.UnknownHostException;

import android.content.ContentUris;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.util.MongoHelper;
import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.data.MongoBrowserProvider;
import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;
import com.innodroid.mongobrowser.util.UiUtils.ConfirmCallbacks;

import butterknife.Bind;
import butterknife.OnClick;

public class ConnectionDetailFragment extends BaseFragment implements LoaderCallbacks<Cursor> {
	@Bind(R.id.connection_detail_title) TextView mTitle;
	@Bind(R.id.connection_detail_server) TextView mServer;
	@Bind(R.id.connection_detail_port) TextView mPort;
	@Bind(R.id.connection_detail_db) TextView mDB;
	@Bind(R.id.connection_detail_user) TextView mUser;
	@Bind(R.id.connection_detail_last_connect) TextView mLastConnect;

	private long mConnectionID;

    public ConnectionDetailFragment() {
    }

	@NonNull
	public static ConnectionDetailFragment newInstance(long id) {
		Bundle arguments = new Bundle();
		arguments.putLong(Constants.ARG_CONNECTION_ID, id);
		ConnectionDetailFragment fragment = new ConnectionDetailFragment();
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public int getTitleText() {
		return R.string.connection_detail;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);

    	mConnectionID = getArguments().getLong(Constants.ARG_CONNECTION_ID);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = super.onCreateView(R.layout.fragment_connection_detail, inflater, container, savedInstanceState);

        getLoaderManager().initLoader(0, getArguments(), this);

        return view;
    }

	@OnClick(R.id.connection_detail_connect)
	public void clickConnect() {
		new ConnectTask().execute();
	}

	@OnClick(R.id.fab_edit)
	public void clickEdit() {
		editConnection();
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.connection_detail_menu, menu);
    }    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
    		case R.id.menu_connection_detail_delete:
    			deleteConnection();
    			return true;
        }

    	return super.onOptionsItemSelected(item);
    }

	public Loader<Cursor> onCreateLoader(int arg0, Bundle args) {
		Uri uri = ContentUris.withAppendedId(MongoBrowserProvider.CONNECTION_URI, mConnectionID);
	    return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Resources res = getResources();
		
		if (!cursor.moveToFirst())
			return;
		
		mTitle.setText(cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_NAME));
		mServer.setText(res.getString(R.string.server) + " : " + cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_SERVER));
		mPort.setText(res.getString(R.string.port) + " : " + cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_PORT));
		mDB.setText(res.getString(R.string.database) + " : " + cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_DB));
		mUser.setText(res.getString(R.string.user) + " : " + cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_USER));
		
		long lastConnect = cursor.getLong(MongoBrowserProvider.INDEX_CONNECTION_LAST_CONNECT);
		if (lastConnect == 0)
			mLastConnect.setText(getResources().getString(R.string.never_connected));
		else
			mLastConnect.setText(String.format(getResources().getString(R.string.last_connected), DateUtils.getRelativeTimeSpanString(lastConnect)));
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
	}
	
    private void editConnection() {
        DialogFragment fragment = ConnectionEditDialogFragment.newInstance(mConnectionID);
        fragment.show(getFragmentManager(), null);
    }

    private void deleteConnection() {
    	UiUtils.confirm(getActivity(), R.string.confirm_delete_connection, new ConfirmCallbacks() {
			@Override
			public boolean onConfirm() {
            	new DeleteConnectionTask().execute();
				return true;
			}
    	});
    }

	public void onEvent(Events.ConnectionUpdated e) {
		// Gets detached on orientation change
		if (super.isAdded())
			getLoaderManager().initLoader(0, getArguments(), this);
	}

	private class ConnectTask extends SafeAsyncTask<Void, Void, Boolean>{
		public ConnectTask() {
			super(getActivity());
		}
		
		@Override
		protected Boolean safeDoInBackground(Void... arg0) throws UnknownHostException {
			Uri uri = ContentUris.withAppendedId(MongoBrowserProvider.CONNECTION_URI, mConnectionID);
			Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
			cursor.moveToFirst();
			
	    	String server = cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_SERVER);
	    	int port = cursor.getInt(MongoBrowserProvider.INDEX_CONNECTION_PORT);
	    	String database = cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_DB);
	    	String user = cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_USER);
	    	String password = cursor.getString(MongoBrowserProvider.INDEX_CONNECTION_PASSWORD); 
			
	    	MongoHelper.connect(server, port, database, user, password);
	    	new MongoBrowserProviderHelper(getActivity().getContentResolver()).updateConnectionLastConnect(mConnectionID);
	    	
			return true;
		}
		
		@Override
		protected void safeOnPostExecute(Boolean result) {
			Events.postConnected(mConnectionID);
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Connect";
		}
		
		@Override
		protected String getProgressMessage() {
			return "Connecting";
		}
	}
	
    private class DeleteConnectionTask extends SafeAsyncTask<Void, Void, Boolean> {
    	public DeleteConnectionTask() {
			super(getActivity());
		}

		@Override
		protected Boolean safeDoInBackground(Void... arg0) {
			new MongoBrowserProviderHelper(getActivity().getContentResolver()).deleteConnection(mConnectionID);
			return true;
		}
		
		@Override
		protected void safeOnPostExecute(Boolean result) {
			Events.postConnectionDeleted();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Delete";
		}
    }
}
