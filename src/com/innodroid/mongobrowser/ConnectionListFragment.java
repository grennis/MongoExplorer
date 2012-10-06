package com.innodroid.mongobrowser;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.Toast;

import com.innodroid.mongobrowser.data.MongoBrowserProvider;
import com.innodroid.mongobrowser.data.MongoConnectionAdapter;

public class ConnectionListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private MongoConnectionAdapter mAdapter;
    private Callbacks mCallbacks = null;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {
        public void onItemSelected(long id);
    }

    public ConnectionListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mAdapter = new MongoConnectionAdapter(getActivity(), null, true);
		setListAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
       
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	setupContextActionBar();
    }
    
    @TargetApi(11)
	private void setupContextActionBar() {
        ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
	        @Override
	        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
	            // Here you can do something when items are selected/de-selected,
	            // such as update the title in the CAB
	        }
	
	        @Override
	        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	            // Respond to clicks on the actions in the CAB
	            switch (item.getItemId()) {
	                case R.id.connection_list_menu_delete:
	                    Toast.makeText(getActivity(), "delete", Toast.LENGTH_SHORT).show();
	                    mode.finish(); // Action picked, so close the CAB
	                    return true;
	                default:
	                    return false;
	            }
	        }
	
	        @Override
	        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	            // Inflate the menu for the CAB
	            MenuInflater inflater = mode.getMenuInflater();
	            inflater.inflate(R.menu.connection_list_context, menu);
	            return true;
	        }
	
	        @Override
	        public void onDestroyActionMode(ActionMode mode) {
	            // Here you can make any necessary updates to the activity when
	            // the CAB is removed. By default, selected items are deselected/unchecked.
	        }
	
	        @Override
	        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	            // Here you can perform updates to the CAB due to
	            // an invalidate() request
	            return false;
	        }
        });
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageReceiver, new IntentFilter(Constants.MessageRefreshConnectionList));

        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        mCallbacks = null;
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        
        if (mCallbacks != null)
        	mCallbacks.onItemSelected(mAdapter.getItemId(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
	public Loader<Cursor> onCreateLoader(int id, Bundle params) {
	    return new CursorLoader(getActivity(), MongoBrowserProvider.CONNECTION_URI, null, null, null, MongoBrowserProvider.NAME_CONNECTION_NAME);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			getLoaderManager().initLoader(0, null, ConnectionListFragment.this);
		}
	};
}
