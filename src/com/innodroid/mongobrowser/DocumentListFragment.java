package com.innodroid.mongobrowser;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.innodroid.mongobrowser.data.MongoCollectionAdapter;

public class DocumentListFragment extends ListFragment implements EditCollectionDialogFragment.Callbacks {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private MongoCollectionAdapter mAdapter;
    private Callbacks mCallbacks = null;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    public static final String ARG_COLLECTION_NAME = "coll_name";

    public interface Callbacks {
        public void onDocumentItemSelected(long id);
        public void onCollectionEdited(String name);
    }

    public DocumentListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mAdapter = new MongoCollectionAdapter(getActivity());
		setListAdapter(mAdapter);
		
		setHasOptionsMenu(true);

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
        }

    	return super.onOptionsItemSelected(item);
    }

    
    private void editCollection() {
    	String name = getArguments().getString(ARG_COLLECTION_NAME);
        DialogFragment fragment = EditCollectionDialogFragment.create(-1, name, this);
        fragment.show(getFragmentManager(), null);
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
		mCallbacks.onCollectionEdited(name);
	}

//    private class LoadNamesTask extends AsyncTask<Void, Void, String[]> {
//		@Override
//		protected String[] doInBackground(Void... arg0) {
//			boolean includeSystem = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Constants.PrefShowSystemCollections, false);
//			return MongoHelper.getCollectionNames(includeSystem);
//		}		
//		
//		@Override
//		protected void onPostExecute(String[] result) {
//			super.onPostExecute(result);
//
//			mAdapter.loadItems(result);
//		}
//    }
}
