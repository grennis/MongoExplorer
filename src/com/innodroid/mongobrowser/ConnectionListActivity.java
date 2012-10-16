package com.innodroid.mongobrowser;

import android.content.Intent;
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
import com.innodroid.mongobrowser.util.SafeAsyncTask;

public class ConnectionListActivity extends FragmentActivity implements ConnectionListFragment.Callbacks, ConnectionDetailFragment.Callbacks, CollectionListFragment.Callbacks, DocumentListFragment.Callbacks, ConnectionEditDialogFragment.Callbacks, DocumentDetailFragment.Callbacks, DocumentEditDialogFragment.Callbacks {
	private static final String STATE_COLLECTION_NAME = "collname";
	
	private boolean mTwoPane;
	private String mCollectionName;
    private FrameLayout mFrame1;
    private FrameLayout mFrame2;
    private FrameLayout mFrame3;
    private FrameLayout mFrame4;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connection_list);

        mFrame1 = (FrameLayout)findViewById(R.id.frame_1);
        mFrame2 = (FrameLayout)findViewById(R.id.frame_2);
        mFrame3 = (FrameLayout)findViewById(R.id.frame_3);
        mFrame4 = (FrameLayout)findViewById(R.id.frame_4);

        if (mFrame2 != null)
            mTwoPane = true;

        if (mTwoPane)
        	setTitle(R.string.app_name);
        else
        	setTitle(R.string.title_connection_list);

        if (savedInstanceState == null) {
        	loadConnectionListPane();
        } else {
        	mCollectionName = savedInstanceState.getString(STATE_COLLECTION_NAME);
        		
        	int depth = getSupportFragmentManager().getBackStackEntryCount();
        	if (depth == 2) {
        		mFrame1.setVisibility(View.GONE);
        		mFrame2.setVisibility(View.GONE);
        		mFrame3.setVisibility(View.VISIBLE);
        		mFrame4.setVisibility(View.VISIBLE);        		
        	} else if (depth == 1) {
        		mFrame1.setVisibility(View.GONE);
        		mFrame3.setVisibility(View.VISIBLE);
        	}        		
        }

        new AddConnectionIfNoneExistTask().execute();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_COLLECTION_NAME, mCollectionName);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
				hideDocumentDetailPane();
				return true;
			}
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				hideDocumentListPane();
				return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
    public void onConnectionItemSelected(int position, long id) {
        if (mTwoPane) {
        	loadConnectionDetailsPane(id);
        } else {
        	showDetailsActivity(id);
        }
    }
	
	private void loadConnectionListPane() {
    	Bundle args = new Bundle();
        ConnectionListFragment fragment = new ConnectionListFragment();
        args.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, mTwoPane);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_1, fragment).commit();		
	}
	
    private void loadConnectionDetailsPane(long id) {
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

    	if (!alreadyShiftedFrames)
    		mFrame3.setVisibility(View.VISIBLE);

    	Bundle arguments = new Bundle();
        DocumentListFragment fragment = new DocumentListFragment();
        arguments.putString(Constants.ARG_COLLECTION_NAME, collection);
        arguments.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, true);
        fragment.setArguments(arguments);

    	Fragment connectionList = fm.findFragmentById(R.id.frame_1);
    	FragmentTransaction ft = fm.beginTransaction();
    	ft.replace(R.id.frame_3, fragment);

    	if (!alreadyShiftedFrames) {
    		ft.remove(connectionList);
        	ft.addToBackStack("doclist");
    	}
    	
    	ft.commit();    	

    	if (!alreadyShiftedFrames)
    		mFrame1.setVisibility(View.GONE);
    }
    
    private void loadDocumentDetailsPane(String content) {
        FragmentManager fm = getSupportFragmentManager();
    	boolean alreadyShiftedFrames = fm.getBackStackEntryCount() > 1;

    	if (!alreadyShiftedFrames)
    		mFrame4.setVisibility(View.VISIBLE);

    	Bundle arguments = new Bundle();
        DocumentDetailFragment fragment = new DocumentDetailFragment();
        arguments.putString(Constants.ARG_COLLECTION_NAME, mCollectionName);
        arguments.putString(Constants.ARG_DOCUMENT_CONTENT, content);
        arguments.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, true);
        fragment.setArguments(arguments);

    	Fragment collectionList = fm.findFragmentById(R.id.frame_2);
    	FragmentTransaction ft = fm.beginTransaction();
    	ft.replace(R.id.frame_4, fragment);

    	if (!alreadyShiftedFrames) {
    		ft.remove(collectionList);
        	ft.addToBackStack("docdetails");
    	}
    	
    	ft.commit();    	
    	
    	if (!alreadyShiftedFrames)
    		mFrame2.setVisibility(View.GONE);
    }

    private void hideDocumentListPane() {
    	mFrame1.setVisibility(View.VISIBLE);    
    	getSupportFragmentManager().popBackStack();
    	mFrame3.setVisibility(View.GONE);
    }

    private void hideDocumentDetailPane() {
    	mFrame2.setVisibility(View.VISIBLE);    	
    	getSupportFragmentManager().popBackStack();
    	
    	// Pop the back stack isnt really enough since the fragment added in the transaction may have been replaced
    	FragmentManager fm = getSupportFragmentManager();
    	fm.beginTransaction().remove(fm.findFragmentById(R.id.frame_4)).commit();
    	
    	mFrame4.setVisibility(View.GONE);
    }

    @Override
    public void onAddConnection() {
    	addConnection();
    }

    private void addConnection() {
        DialogFragment fragment = ConnectionEditDialogFragment.create(0, this);
        fragment.show(getSupportFragmentManager(), null);
    }

	@Override
	public void onCollectionItemSelected(String name) {
		mCollectionName = name;
		loadDocumentListPane(name);
	}

	@Override
	public void onDocumentItemSelected(String content) {
		loadDocumentDetailsPane(content);
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
	public void onConnectionAdded(long id) {
		reloadConnectionListAndSelect(id);
	}

	@Override
	public void onConnectionUpdated(long id) {
		reloadConnectionListAndSelect(id);
	}

	private void reloadConnectionListAndSelect(long id) {
		ConnectionListFragment fragment = (ConnectionListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);
        fragment.reloadAndSelect(id);
        
        if (mTwoPane)
        	loadConnectionDetailsPane(id);
	}

	@Override
	public void onCollectionEdited(String name) {
        CollectionListFragment fragment = (CollectionListFragment)getSupportFragmentManager().findFragmentById(R.id.frame_2);
        fragment.onCollectionEdited(name);		
	}

	@Override
	public void onCollectionDropped(String name) {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 1) {
        	hideDocumentDetailPane();
        }
        
        fm.beginTransaction().remove(fm.findFragmentById(R.id.frame_3)).commit();

    	CollectionListFragment fragment = (CollectionListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_2);
        fragment.onCollectionDropped();
	}
	
	@Override
	public void onAddDocument() {
		DocumentEditDialogFragment fragment = DocumentEditDialogFragment.create(mCollectionName, true, Constants.NEW_DOCUMENT_CONTENT_PADDED, this);
		fragment.show(getSupportFragmentManager(), null);
	}

	@Override
	public void onEditDocument(String content) {
		DocumentEditDialogFragment fragment = DocumentEditDialogFragment.create(mCollectionName, false, content, this);
		fragment.show(getSupportFragmentManager(), null);
	}

	@Override
	public void onDocumentCreated(String content) {
        DocumentListFragment fragment = (DocumentListFragment)getSupportFragmentManager().findFragmentById(R.id.frame_3);
        fragment.onDocumentCreated(content);
	}

	@Override
	public void onDocumentUpdated(String content) {
        DocumentListFragment fragment = (DocumentListFragment)getSupportFragmentManager().findFragmentById(R.id.frame_3);
        fragment.onDocumentUpdated(content);
	}

	@Override
	public void onDeleteDocument() {
        DocumentListFragment fragment = (DocumentListFragment)getSupportFragmentManager().findFragmentById(R.id.frame_3);
        fragment.onDocumentDeleted();
        
        if (fragment.getItemCount() == 0)
        	hideDocumentDetailPane();
	}

    private class AddConnectionIfNoneExistTask extends SafeAsyncTask<Void, Void, Boolean> {
		public AddConnectionIfNoneExistTask() {
			super(ConnectionListActivity.this);
		}

		@Override
		protected Boolean safeDoInBackground(Void... arg0) {
			return new MongoBrowserProviderHelper(getContentResolver()).getConnectionCount() == 0;
		}

		@Override
		protected void safeOnPostExecute(Boolean res) {
			if (res)
				addConnection();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Check Connections";
		}
    }
}

