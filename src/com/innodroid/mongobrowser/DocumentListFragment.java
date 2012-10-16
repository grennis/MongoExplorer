package com.innodroid.mongobrowser;


import java.net.UnknownHostException;

import android.app.Activity;
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

import com.innodroid.mongo.MongoHelper;
import com.innodroid.mongobrowser.data.MongoDocumentAdapter;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;
import com.innodroid.mongobrowser.util.UiUtils.AlertDialogCallbacks;

public class DocumentListFragment extends ListFragment implements CollectionEditDialogFragment.Callbacks {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private String mCollectionName;
    private MongoDocumentAdapter mAdapter;
    private Callbacks mCallbacks = null;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private int mStart = 0;
    private int mTake = 5;

    public interface Callbacks {
    	public void onAddDocument();
        public void onDocumentItemSelected(String content);
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

		int take = getResources().getInteger(R.integer.default_document_page_size);
		mTake = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Constants.PrefDocumentPageSize, take);
    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME);

		new LoadNextDocumentsTask().execute();
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
    public boolean onOptionsItemSelected(MenuItem item) {    	
        switch (item.getItemId()) {
    		case R.id.menu_document_list_add:
    			mCallbacks.onAddDocument();
    			return true;
    		case R.id.menu_document_list_edit:
    			editCollection();
    			return true;
    		case R.id.menu_document_list_delete:
    			dropCollection();
    			return true;
    		default:
    	    	return super.onOptionsItemSelected(item);
        }
    }

	public void onDocumentCreated(String content) {
		mAdapter.insert(0, content);
		setActivatedPosition(0);
		mCallbacks.onDocumentItemSelected(content);
	}
	
	public void onDocumentUpdated(String content) {
		mAdapter.update(mActivatedPosition, content);
		mCallbacks.onDocumentItemSelected(content);
	}

	public void onDocumentDeleted() {
		mAdapter.delete(mActivatedPosition);

		if (mActivatedPosition < mAdapter.getActualCount())
			mCallbacks.onDocumentItemSelected(mAdapter.getItem(mActivatedPosition));
		else {
			mCallbacks.onDocumentItemSelected(null);
			mActivatedPosition = ListView.INVALID_POSITION;
		}
	}
	
	private void editCollection() {
        DialogFragment fragment = CollectionEditDialogFragment.create(mCollectionName, false, this);
        fragment.show(getFragmentManager(), null);
	}

    private void dropCollection() {
    	UiUtils.confirm(getActivity(), R.string.confirm_drop_collection, new AlertDialogCallbacks() {
			@Override
			public boolean onOK() {				
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
    	UiUtils.confirm(getActivity(), R.string.really_confirm_drop_collection, new AlertDialogCallbacks() {
			@Override
			public boolean onOK() {				
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
        	new LoadNextDocumentsTask().execute();
        } else {
        	setActivatedPosition(position);
        	
        	if (mCallbacks != null)
        		mCallbacks.onDocumentItemSelected(mAdapter.getItem(position));
        }
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
    	public LoadNextDocumentsTask() {
			super(getActivity());
		}

		@Override
		protected String[] safeDoInBackground(Void... args) {
			if (mCollectionName == null)
				return new String[0];
			
			String[] docs = MongoHelper.getPageOfDocuments(mCollectionName, mStart, mTake);
			return docs;
		}

		@Override
		protected void safeOnPostExecute(String[] results) {
			mAdapter.addAll(results);
			
			if (results.length < mTake)
				mAdapter.showLoadMore(false);			
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Load";
		}		
    }
}
