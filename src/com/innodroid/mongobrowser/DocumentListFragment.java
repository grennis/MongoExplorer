package com.innodroid.mongobrowser;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.innodroid.mongo.MongoHelper;
import com.innodroid.mongobrowser.UiUtils.AlertDialogCallbacks;
import com.innodroid.mongobrowser.data.MongoCollectionAdapter;

public class DocumentListFragment extends ListFragment implements EditCollectionDialogFragment.Callbacks {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private String mCollectionName;
    private MongoCollectionAdapter mAdapter;
    private Callbacks mCallbacks = null;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {
        public void onDocumentItemSelected(long id);
        public void onCollectionEdited(String name);
        public void onCollectionDropped(String name);
    }

    public DocumentListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mAdapter = new MongoCollectionAdapter(getActivity());
		setListAdapter(mAdapter);
		
		setHasOptionsMenu(true);

    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME);

		//new LoadNamesTask().execute();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        //LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
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
    		case R.id.document_list_menu_add:
    			return true;
    		case R.id.document_list_menu_edit:
    			editCollection();
    			return true;
    		case R.id.document_list_menu_delete:
    			dropCollection();
    			return true;
    		default:
    	    	return super.onOptionsItemSelected(item);
        }
    }

    
	private void editCollection() {
        DialogFragment fragment = EditCollectionDialogFragment.create(mCollectionName, this);
        fragment.show(getFragmentManager(), null);
	}

    private void dropCollection() {
    	UiUtils.confirm(getActivity(), R.string.confirm_drop_collection, new AlertDialogCallbacks() {
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
        
        if (mCallbacks != null)
        	mCallbacks.onDocumentItemSelected(mAdapter.getItemId(position));
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

	@Override
	public void onCollectionEdited(int pos, String name) {
		new RenameCollectionTask().execute(name);
	}
	
    private class RenameCollectionTask extends AsyncTask<String, Void, String> {
    	private Exception mException;
    	
		@Override
		protected String doInBackground(String... args) {
			try {
				MongoHelper.renameCollection(mCollectionName, args[0]);
				return args[0];
			} catch (Exception ex) {
				mException = ex;
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (mException == null) {
				mCollectionName = result;
				mCallbacks.onCollectionEdited(result);
			} else {
				Toast.makeText(getActivity(), mException.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}		
    }

    private class DropCollectionTask extends AsyncTask<Void, Void, Void> {
    	private Exception mException;
    	
		@Override
		protected Void doInBackground(Void... args) {
			try {
				MongoHelper.dropCollection(mCollectionName);
			} catch (Exception ex) {
				mException = ex;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (mException == null) {
				mCallbacks.onCollectionDropped(mCollectionName);
			} else {
				Toast.makeText(getActivity(), mException.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}		
    }
}
