package com.innodroid.mongobrowser;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

import com.innodroid.mongo.MongoHelper;
import com.innodroid.mongobrowser.data.MongoCollectionAdapter;

public class CollectionListFragment extends ListFragment { //implements LoaderCallbacks<Cursor> {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private boolean mActivateOnItemClick;
    private MongoCollectionAdapter mAdapter;
    private Callbacks mCallbacks = null;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {
        public void onCollectionItemSelected(String name);
    }

    public CollectionListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mAdapter = new MongoCollectionAdapter(getActivity());
		setListAdapter(mAdapter);
		setHasOptionsMenu(true);

		new LoadNamesTask().execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.collection_list_menu, menu);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        
        getListView().setChoiceMode(mActivateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageReceiver, new IntentFilter(Constants.MessageConnectionItemChanged));

        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        //LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        mCallbacks = null;
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        
        if (mCallbacks != null)
        	mCallbacks.onCollectionItemSelected(mAdapter.getCollectionName(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }
    
    public void setActivateOnItemClick(boolean activateOnItemClick) {
    	mActivateOnItemClick = true;
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
//	public Loader<Cursor> onCreateLoader(int id, Bundle params) {
//	    return new CursorLoader(getActivity(), MongoBrowserProvider.CONNECTION_URI, null, null, null, MongoBrowserProvider.NAME_CONNECTION_NAME);
//	}
//
//	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//		mAdapter.swapCursor(cursor);
//	}
//
//	public void onLoaderReset(Loader<Cursor> loader) {
//		mAdapter.swapCursor(null);
//	}

//	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			getLoaderManager().initLoader(0, null, CollectionListFragment.this);
//		}
//	};
    
    private class LoadNamesTask extends AsyncTask<Void, Void, String[]> {
		@Override
		protected String[] doInBackground(Void... arg0) {
			boolean includeSystem = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Constants.PrefShowSystemCollections, false);
			return MongoHelper.getCollectionNames(includeSystem);
		}		
		
		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);

			mAdapter.loadItems(result);
			
			new LoadCountsTask().execute(result);
		}
    }

    private class LoadCountsTask extends AsyncTask<String, Long, Void> {
		@Override
		protected Void doInBackground(String... arg0) {
			for (int i = 0; i<arg0.length; i++) {
				publishProgress(new Long[] { (long)i, MongoHelper.getCollectionCount(arg0[i])});
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Long... values) {
			super.onProgressUpdate(values);
			
			int index = (int)(long)values[0];
			mAdapter.setItemCount(index, values[1]);
		}
    }
}
