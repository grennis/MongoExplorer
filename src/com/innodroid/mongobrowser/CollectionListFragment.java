package com.innodroid.mongobrowser;


import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.innodroid.mongo.MongoHelper;
import com.innodroid.mongobrowser.data.MongoCollectionAdapter;
import com.innodroid.mongobrowser.util.SafeAsyncTask;

public class CollectionListFragment extends ListFragment implements CollectionEditDialogFragment.Callbacks {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

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
    }

    @Override
    public void onResume() {
    	super.onResume();

    	refreshList();
    }
    
	public void refreshList() {
    	//mSelectAfterLoad = selectAfterLoad;
    	new LoadNamesTask().execute();
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.collection_list_menu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_collection_list_add:
            	addCollection();
                return true;
        }

    	return super.onOptionsItemSelected(item);
    }
    
    private void addCollection() {
        DialogFragment fragment = CollectionEditDialogFragment.create("", this);
        fragment.show(getFragmentManager(), null);
	}
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        
        getListView().setChoiceMode(getArguments().getBoolean(Constants.ARG_ACTIVATE_ON_CLICK)
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
    
    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
	@Override
	public void onCollectionEdited(int pos, String name) {
		new AddCollectionTask().execute(name);
	}

    private class AddCollectionTask extends SafeAsyncTask<String, Void, String> {
    	public AddCollectionTask() {
			super(getActivity());
		}

    	@Override
		protected String safeDoInBackground(String... args) throws Exception {
			MongoHelper.createCollection(args[0]);
			return args[0];
		}

		@Override
		protected void safeOnPostExecute(String result) {
			mAdapter.add(result);
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Add";
		}		
    }
	
    private class LoadNamesTask extends SafeAsyncTask<Void, Void, String[]> {
    	public LoadNamesTask() {
			super(getActivity());
		}

    	@Override
		protected String[] safeDoInBackground(Void... arg0) {
			boolean includeSystem = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Constants.PrefShowSystemCollections, false);
			return MongoHelper.getCollectionNames(includeSystem);
		}		
		
		@Override
		protected void safeOnPostExecute(String[] result) {
			mAdapter.loadItems(result);
			new LoadCountsTask().execute(result);
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Get Names";
		}
		
		@Override
		protected String getProgressMessage() {
			return "Loading";
		}		
    }

    private class LoadCountsTask extends SafeAsyncTask<String, Long, Void> {
    	public LoadCountsTask() {
			super(getActivity());
		}

    	@Override
		protected Void safeDoInBackground(String... arg0) {
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
		
		@Override
		protected void safeOnPostExecute(Void result) {
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Get Counts";
		}
    }
}
