package com.innodroid.mongobrowser.ui;


import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.data.MongoCollection;
import com.innodroid.mongobrowser.data.MongoData;
import com.innodroid.mongobrowser.util.MongoHelper;
import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.adapters.MongoCollectionAdapter;
import com.innodroid.mongobrowser.util.Preferences;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;

import butterknife.OnClick;
import butterknife.OnItemClick;

public class CollectionListFragment extends BaseListFragment {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private long mConnectionId;
    private MongoCollectionAdapter mAdapter;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public CollectionListFragment() {
    }

	@NonNull
	public static CollectionListFragment newInstance(long connectionId, boolean activateOnClick) {
		Bundle arguments = new Bundle();
		CollectionListFragment fragment = new CollectionListFragment();
		arguments.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, activateOnClick);
		arguments.putLong(Constants.ARG_CONNECTION_ID, connectionId);
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public int getTitleText() {
		return R.string.collections;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConnectionId = getArguments().getLong(Constants.ARG_CONNECTION_ID);
		setHasOptionsMenu(true);

		if (savedInstanceState != null) {
			mActivatedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		if (mAdapter == null) {
			mAdapter = new MongoCollectionAdapter(getActivity());
			onRefresh();
		} else {
			mAdapter.notifyDataSetChanged();
		}

		mList.setAdapter(mAdapter);

		mList.setChoiceMode(mActivateOnClick
				? ListView.CHOICE_MODE_SINGLE
				: ListView.CHOICE_MODE_NONE);

		if (mActivatedPosition != ListView.INVALID_POSITION) {
			setActivatedPosition(mActivatedPosition);
		}

		return view;
	}

	public void onEvent(Events.DocumentCreated e) {
		onRefresh();
	}

	public void onEvent(Events.DocumentDeleted e) {
		onRefresh();
	}

	public void onEvent(Events.SettingsChanged e) {
		onRefresh();
	}

	@OnClick(R.id.fab_add)
	public void clickAdd() {
		addCollection();
	}

	@Override
	public void onRefresh() {
    	new LoadNamesTask().execute();
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.collection_list_menu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_collection_list_change_db:
            	changeDatabase();
                return true;
        }

    	return super.onOptionsItemSelected(item);
    }
    
    private void addCollection() {
        DialogFragment fragment = CollectionEditDialogFragment.newInstance(-1);
        fragment.show(getFragmentManager(), null);
	}

	private void insertCollection(String name) {
		mAdapter.add(0, name);

		if (mActivateOnClick) {
			setActivatedPosition(0);
			Events.postCollectionSelected(mConnectionId, 0);
		}
	}

    private void changeDatabase() {
    	new GetDatabasesTask().execute();
    }

	@OnItemClick(android.R.id.list)
    public void onItemClick(int position) {
        setActivatedPosition(position);

		Events.postCollectionSelected(mConnectionId, position);
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
            mList.setItemChecked(mActivatedPosition, false);
        } else {
			mList.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

	public void onEvent(Events.CreateCollection e) {
		new AddCollectionTask().execute(e.Name);
	}

	public void onEvent(Events.CollectionRenamed e) {
		mAdapter.notifyDataSetChanged();
	}
	
	public void onEvent(Events.ChangeDatabase e) {
		new ChangeDatabaseTask().execute(e.Name);
	}

	public void onEvent(Events.CollectionDropped e) {
		mAdapter.notifyDataSetChanged();

		if (mActivateOnClick && mActivatedPosition < mAdapter.getCount())
			Events.postCollectionSelected(mConnectionId, mActivatedPosition);
		else {
			Events.postCollectionSelected(mConnectionId, -1);
			mActivatedPosition = ListView.INVALID_POSITION;
		}
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
			insertCollection(result);
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Add";
		}		
    }
	
    private class LoadNamesTask extends SafeAsyncTask<Void, Void, List<MongoCollection>> {
		boolean mShowSystemCollections;

    	public LoadNamesTask() {
			super(getActivity());

			mShowSystemCollections = new Preferences(getActivity()).getShowSystemCollections();
		}

    	@Override
		protected List<MongoCollection> safeDoInBackground(Void... arg0) {
			return MongoHelper.getCollectionNames(mShowSystemCollections);
		}

		@Override
		protected void safeOnPostExecute(List<MongoCollection> result) {
			if (mSwipeRefresh != null) {
				mSwipeRefresh.setRefreshing(false);
			}

			if (mAdapter != null) {
				MongoData.Collections = result;
				mAdapter.setItems(result);
				new LoadCountsTask(result).execute();
			}
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Get Names";
		}		
    }

    private class LoadCountsTask extends SafeAsyncTask<Void, Integer, Void> {
		private List<MongoCollection> mCollections;

		public LoadCountsTask(List<MongoCollection> list) {
			super(getActivity());

			mCollections = list;
		}

    	@Override
		protected Void safeDoInBackground(Void... voids) {
			for (int i = 0; i<mCollections.size(); i++) {
				long count = MongoHelper.getCollectionCount(mCollections.get(i).Name);
				mCollections.get(i).Count = count;
				publishProgress(i);
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			
			mAdapter.notifyCountChanged(values[0]);
		}
		
		@Override
		protected void safeOnPostExecute(Void result) {
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Get Counts";
		}
    }
    
    private class GetDatabasesTask extends SafeAsyncTask<String, Void, ArrayList<String>> {
    	public GetDatabasesTask() {
			super(getActivity());
		}

    	@Override
		protected ArrayList<String> safeDoInBackground(String... args) throws Exception {
			return MongoHelper.getDatabaseNames();
		}

		@Override
		protected void safeOnPostExecute(ArrayList<String> result) {
			if (result == null || result.size() == 0) {
				UiUtils.message(getActivity(), R.string.title_no_databases, R.string.message_no_databases);
				return;
			}

	        DialogFragment fragment = ChangeDatabaseDialogFragment.newInstance(result);
	        fragment.show(getFragmentManager(), null);
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to get database list";
		}		
    }
    
    private class ChangeDatabaseTask extends SafeAsyncTask<String, Void, String> {
    	public ChangeDatabaseTask() {
			super(getActivity());
		}

    	@Override
		protected String safeDoInBackground(String... args) throws Exception {
    		MongoHelper.changeDatabase(args[0]);
    		return args[0];
		}

		@Override
		protected void safeOnPostExecute(String result) {
			onRefresh();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Change DB";
		}		
    }
}
