package com.innodroid.mongobrowser;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;

public class ConnectionListActivity extends FragmentActivity implements ConnectionListFragment.Callbacks, ConnectionDetailFragment.Callbacks, CollectionListFragment.Callbacks, DocumentListFragment.Callbacks, EditConnectionDialogFragment.Callbacks {
	private static final String STATE_NAV_DEPTH = "navdepth";
	private boolean mTwoPane;
    private FrameLayout mFrame1;
    private FrameLayout mFrame2;
    private FrameLayout mFrame3;
    private FrameLayout mFrame4;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_connection_list);
        setContentView(R.layout.activity_connection_list);

        mFrame1 = (FrameLayout)findViewById(R.id.frame_1);
        mFrame2 = (FrameLayout)findViewById(R.id.frame_2);
        mFrame3 = (FrameLayout)findViewById(R.id.frame_3);
        mFrame4 = (FrameLayout)findViewById(R.id.frame_4);

        if (mFrame2 != null)
            mTwoPane = true;

        if (savedInstanceState == null) {
        	Bundle args = new Bundle();
	        ConnectionListFragment fragment = new ConnectionListFragment();
	        args.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, mTwoPane);
	        fragment.setArguments(args);
	        getSupportFragmentManager().beginTransaction()
	                .replace(R.id.frame_1, fragment)
	                .commit();
        } else {
        	if (savedInstanceState.getInt(STATE_NAV_DEPTH) > 0) {
            	mFrame1.setVisibility(View.GONE);
            	mFrame4.setVisibility(View.VISIBLE);
        	}
        }

        new AddConnectionIfNoneExistTask().execute();
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(STATE_NAV_DEPTH, getSupportFragmentManager().getBackStackEntryCount());
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				hideDocumentListPane();
				return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
    public void onConnectionItemSelected(long id) {
        if (mTwoPane) {
        	loadDetailsPane(id);
        } else {
        	showDetailsActivity(id);
        }
    }
	
    private void loadDetailsPane(long id) {
        Bundle arguments = new Bundle();
        arguments.putLong(Constants.ARG_CONNECTION_ID, id);
        ConnectionDetailFragment fragment = new ConnectionDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_2, fragment)
                .commit();
	}
    
    private void showDetailsActivity(long id) {
        Intent detailIntent = new Intent(this, ConnectionDetailActivity.class);
        detailIntent.putExtra(Constants.ARG_CONNECTION_ID, id);
        startActivity(detailIntent);
    }
    
    private void loadCollectionListPane() {
        Bundle arguments = new Bundle();
        CollectionListFragment fragment = new CollectionListFragment();
        arguments.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, true);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_2, fragment)
                .commit();
    }

    private void loadDocumentListPane(String collection) {
        FragmentManager fm = getSupportFragmentManager();
    	boolean alreadyShiftedFrames = fm.getBackStackEntryCount() > 0;

    	mFrame1.setVisibility(View.GONE);
    	mFrame4.setVisibility(View.VISIBLE);

    	Bundle arguments = new Bundle();
        DocumentListFragment fragment = new DocumentListFragment();
        arguments.putString(Constants.ARG_COLLECTION_NAME, collection);
        arguments.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, true);
        fragment.setArguments(arguments);

    	Fragment connectionList = fm.findFragmentById(R.id.frame_1);
    	FragmentTransaction ft = fm.beginTransaction();

    	if (!alreadyShiftedFrames) {
        	ft.addToBackStack("");
    		ft.remove(connectionList);
    	}
    	
    	ft.replace(R.id.frame_3, fragment);
    	ft.commit();    	
    	
    	//invalidateOptionsMenu();
    }

    private void hideDocumentListPane() {
    	mFrame4.setVisibility(View.GONE);
    	mFrame1.setVisibility(View.VISIBLE);
    	
    	getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onAddConnection() {
    	addConnection();
    }

    private void addConnection() {
        DialogFragment fragment = EditConnectionDialogFragment.create(0, this);
        fragment.show(getSupportFragmentManager(), null);
    }

	@Override
	public void onCollectionItemSelected(String name) {
		loadDocumentListPane(name);
	}

	@Override
	public void onDocumentItemSelected(long id) {
	}

	@Override
	public void onConnected() {
		loadCollectionListPane();
	}

	@Override
	public void onConnectionDeleted() {
        getSupportFragmentManager().beginTransaction()
	        .remove(getSupportFragmentManager().findFragmentById(R.id.frame_2))
	        .commit();
	}

	@Override
	public void onConnectionEdited(long id) {
		ConnectionListFragment fragment = (ConnectionListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);
        fragment.refreshList(id);
        
        if (mTwoPane)
        	loadDetailsPane(id);
	}

	@Override
	public void onCollectionEdited(String name) {
	}
	
	@Override
	public void onCollectionDropped(String name) {
        FragmentManager fm = getSupportFragmentManager();
    	fm.beginTransaction().remove(fm.findFragmentById(R.id.frame_3)).commit();

    	CollectionListFragment fragment = (CollectionListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_2);
        fragment.refreshList();
	}

    private class AddConnectionIfNoneExistTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... arg0) {
			return new MongoBrowserProviderHelper(getContentResolver()).getConnectionCount() == 0;
		}

		@Override
		protected void onPostExecute(Boolean res) {
			super.onPostExecute(res);
			
			if (res)
				addConnection();
		}
    }
}
