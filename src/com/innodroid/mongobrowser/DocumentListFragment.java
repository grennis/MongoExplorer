package com.innodroid.mongobrowser;


import java.net.UnknownHostException;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.innodroid.mongo.MongoHelper;
import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;
import com.innodroid.mongobrowser.data.MongoDocumentAdapter;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;
import com.innodroid.mongobrowser.util.UiUtils.ConfirmCallbacks;

public class DocumentListFragment extends ListFragment implements CollectionEditDialogFragment.Callbacks, QueryEditTextDialogFragment.Callbacks, QueryEditNameDialogFragment.Callbacks {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String STATE_QUERY_TEXT = "query_text";

    private String mCollectionName;
    private String mQueryName;
    private String mQueryText;
    private MongoDocumentAdapter mAdapter;
    private Callbacks mCallbacks = null;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private int mStart = 0;
    private int mTake = 5;

    public interface Callbacks {
    	public void onDocumentListRefreshRequested();
    	public void onAddDocument();
        public void onDocumentItemClicked(String content);
        public void onDocumentItemActivated(String content);
        public void onCollectionEdited(String name);
        public void onCollectionDropped(String name);
    }

    public DocumentListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mAdapter = new MongoDocumentAdapter(getActivity());
		setListAdapter(mAdapter);
		
		setHasOptionsMenu(true);
		
		// Need to preserve the state of the list... the user may have hit "load more" a few times and need to preserve that
    	setRetainInstance(true);

		int take = getResources().getInteger(R.integer.default_document_page_size);
		mTake = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Constants.PrefDocumentPageSize, take);
    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME);

		if (savedInstanceState != null) {
			mActivatedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
			mQueryText = savedInstanceState.getString(STATE_QUERY_TEXT);
		}
		
		new LoadNextDocumentsTask(false).execute();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setChoiceMode(getArguments().getBoolean(Constants.ARG_ACTIVATE_ON_CLICK)
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
        
        if (mActivatedPosition != ListView.INVALID_POSITION)
            setActivatedPosition(mActivatedPosition);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        mCallbacks = null;
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.document_list_menu, menu);
    }    

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	boolean haveQuery = mQueryText != null;
		menu.findItem(R.id.menu_document_list_clear_query).setEnabled(haveQuery);
		menu.findItem(R.id.menu_document_list_save_query).setEnabled(haveQuery);
		menu.findItem(R.id.menu_document_list_edit_query).setEnabled(haveQuery);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {    	
        switch (item.getItemId()) {
    		case R.id.menu_document_list_add:
    			mCallbacks.onAddDocument();
    			return true;
    		case R.id.menu_document_list_new_query:
    			newQuery();
    			return true;
    		case R.id.menu_document_list_edit_query:
    			editQuery();
    			return true;
    		case R.id.menu_document_list_save_query:
    			saveQuery();
    			return true;
    		case R.id.menu_document_list_clear_query:
    			clearQuery();
    			return true;
    		case R.id.menu_document_list_edit:
    			editCollection();
    			return true;
    		case R.id.menu_document_list_delete:
    			dropCollection();
    			return true;
    		case R.id.menu_document_list_refresh:
    			mCallbacks.onDocumentListRefreshRequested();
    			return true;
    		default:
    	    	return super.onOptionsItemSelected(item);
        }
    }

	private void newQuery() {
		mQueryName = null;
		mQueryText = null;
		editQuery();
	}
	
    private void editQuery() {
		String query = (mQueryText == null) ? Constants.NEW_DOCUMENT_CONTENT_PADDED : mQueryText;
		QueryEditTextDialogFragment.create(query, this).show(getFragmentManager(), null);
	}

    private void saveQuery() {
    	if (mQueryName == null) {
    		new GetUniqueQueryName().execute();
    		return;
    	}
    	
		QueryEditNameDialogFragment.create(mQueryName, this).show(getFragmentManager(), null);
	}

	public void clearQuery() {
		mQueryName = null;
		mQueryText = null;
		reloadList(true);
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
    		getActivity().invalidateOptionsMenu();
	}
	
	@Override 
	public void onQueryNamed(String name) {
		mQueryName = name;
		new SaveQuery().execute();
	}

	public void onDocumentCreated(String content) {
		mAdapter.insert(0, content);
		setActivatedPosition(0);
		mCallbacks.onDocumentItemActivated(content);
	}
	
	public void onDocumentUpdated(String content) {
		mAdapter.update(mActivatedPosition, content);
		mCallbacks.onDocumentItemActivated(content);
	}

	public void onDocumentDeleted() {
		mAdapter.delete(mActivatedPosition);

		if (mActivatedPosition < mAdapter.getActualCount())
			mCallbacks.onDocumentItemActivated(mAdapter.getItem(mActivatedPosition));
		else {
			mCallbacks.onDocumentItemActivated(null);
			mActivatedPosition = ListView.INVALID_POSITION;
		}
	}
	
	private void editCollection() {
        DialogFragment fragment = CollectionEditDialogFragment.create(mCollectionName, false, this);
        fragment.show(getFragmentManager(), null);
	}

    private void dropCollection() {
    	UiUtils.confirm(getActivity(), R.string.confirm_drop_collection, new ConfirmCallbacks() {
			@Override
			public boolean onConfirm() {				
				if (mAdapter.getCount() == 0) {
					new DropCollectionTask().execute();
					return true;
				}
				
				reconfirmDropCollection();
				return true;
			}
    	});
	}

    private void reconfirmDropCollection() {
    	UiUtils.confirm(getActivity(), R.string.really_confirm_drop_collection, new ConfirmCallbacks() {
			@Override
			public boolean onConfirm() {				
				new DropCollectionTask().execute();
				return true;
			}
    	});
	}

	@Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        
        if ((mAdapter.isShowingLoadMore()) && (position == mAdapter.getCount()-1)) {
        	mStart += mTake;
        	new LoadNextDocumentsTask(false).execute();
        } else {
        	setActivatedPosition(position);
        	
        	if (mCallbacks != null)
        		mCallbacks.onDocumentItemClicked(mAdapter.getItem(position));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
            outState.putString(STATE_QUERY_TEXT, mQueryText);
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
	public void onCreateCollection(String name) {
		Log.e("ERROR", "Shouldnt get here");
	}
    
	@Override
	public void onRenameCollection(String name) {
		new RenameCollectionTask().execute(name);
	}
	
	public int getItemCount() {
		return mAdapter.getActualCount();
	}
	
	public String getItem(int position) {
		return mAdapter.getItem(position);
	}

	public void reloadList(boolean trySelectAfterLoad) {
		boolean selectAfterLoad = trySelectAfterLoad && (mActivatedPosition != ListView.INVALID_POSITION);
		setActivatedPosition(ListView.INVALID_POSITION);
		mCallbacks.onDocumentItemActivated(null);
		mAdapter.removeAll();
		mStart = 0;
		new LoadNextDocumentsTask(selectAfterLoad).execute();		
	}

	@Override
	public void onQueryUpdated(String query) {
		mQueryText = query;
		reloadList(true);
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
    		getActivity().invalidateOptionsMenu();
	}

    private class RenameCollectionTask extends SafeAsyncTask<String, Void, String> {
    	public RenameCollectionTask() {
			super(getActivity());
		}

		@Override
		protected String safeDoInBackground(String... args) throws UnknownHostException {
			MongoHelper.renameCollection(mCollectionName, args[0]);
			return args[0];
		}

		@Override
		protected void safeOnPostExecute(String result) {
			mCollectionName = result;
			mCallbacks.onCollectionEdited(result);
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Rename";
		}		
    }

    private class DropCollectionTask extends SafeAsyncTask<Void, Void, Void> {
    	public DropCollectionTask() {
			super(getActivity());
		}

		@Override
		protected Void safeDoInBackground(Void... args) {
			MongoHelper.dropCollection(mCollectionName);
			return null;
		}

		@Override
		protected void safeOnPostExecute(Void result) {
			mCallbacks.onCollectionDropped(mCollectionName);
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Drop";
		}		
    }

    private class LoadNextDocumentsTask extends SafeAsyncTask<Void, Void, String[]> {
    	private boolean mSelectAfterLoad;
    	
    	public LoadNextDocumentsTask(boolean selectAfterLoad) {
			super(getActivity());
			
			mSelectAfterLoad = selectAfterLoad;
		}

		@Override
		protected String[] safeDoInBackground(Void... args) {
			if (mCollectionName == null)
				return new String[0];
			
			String[] docs = MongoHelper.getPageOfDocuments(mCollectionName, mQueryText, mStart, mTake);
			return docs;
		}

		@Override
		protected void safeOnPostExecute(String[] results) {
			mAdapter.addAll(results);
			
			if (results.length < mTake)
				mAdapter.showLoadMore(false);
			
			if (mSelectAfterLoad && results.length > 0) {
				setActivatedPosition(0);
				mCallbacks.onDocumentItemActivated(results[0]);
			}
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Load";
		}		
    }
    
    private class GetUniqueQueryName extends SafeAsyncTask<Void, Void, String> {
    	public GetUniqueQueryName() {
			super(getActivity());
    	}

		@Override
		protected String safeDoInBackground(Void... args) {
			String name = "Query ";
			MongoBrowserProviderHelper helper = new MongoBrowserProviderHelper(getActivity().getContentResolver());
			
			int i = 1;
			while (true)
			{
				String tryName = name + i;
				Cursor cursor = helper.getQueryByName(tryName);
				boolean taken = cursor.moveToFirst();
				cursor.close();
				
				if (!taken)
					return tryName;
			
				i++;
			}
		}

		@Override
		protected void safeOnPostExecute(String results) {
			mQueryName = results;
			saveQuery();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Get Query Name";
		}		
    }
    
    private class SaveQuery extends SafeAsyncTask<Void, Void, Void> {
    	public SaveQuery() {
			super(getActivity());
    	}

		@Override
		protected Void safeDoInBackground(Void... args) {
			MongoBrowserProviderHelper helper = new MongoBrowserProviderHelper(getActivity().getContentResolver());
			helper.saveQuery(mQueryName, mQueryText);
			return null;
		}

		@Override
		protected void safeOnPostExecute(Void args) {
			Toast.makeText(getActivity(), "Query Saved", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Save";
		}		
    }
}
