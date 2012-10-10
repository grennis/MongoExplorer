package com.innodroid.mongobrowser;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;

public class ConnectionListActivity extends FragmentActivity implements ConnectionListFragment.Callbacks, ConnectionDetailFragment.Callbacks, CollectionListFragment.Callbacks, DocumentListFragment.Callbacks, ConnectionSetupDialogFragment.Callbacks {
    private boolean mTwoPane;
    private boolean mViewsShifted;
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
        
        if (mFrame1 != null) {
            mTwoPane = true;
            ((ConnectionListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_connection_list))
                    .setActivateOnItemClick(true);
        }

        new AddConnectionIfNoneExistTask().execute();
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (mViewsShifted) {
				shiftViewsRight();
				return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
    public void onConnectionItemSelected(long id) {
        if (mTwoPane) {
        	loadDetailsPane(id);
        	invalidateOptionsMenu();
        } else {
            Intent detailIntent = new Intent(this, ConnectionDetailActivity.class);
            detailIntent.putExtra(ConnectionDetailFragment.ARG_CONNECTION_ID, id);
            startActivity(detailIntent);
        }
    }
	
    private void loadDetailsPane(long id) {
        Bundle arguments = new Bundle();
        arguments.putLong(ConnectionDetailFragment.ARG_CONNECTION_ID, id);
        ConnectionDetailFragment fragment = new ConnectionDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_2, fragment)
                .commit();
	}
    
    private void loadCollectionListPane() {
        Bundle arguments = new Bundle();
        CollectionListFragment fragment = new CollectionListFragment();
        fragment.setArguments(arguments);
        fragment.setActivateOnItemClick(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_2, fragment)
                .commit();
    }

    private void loadDocumentListPane() {
        Bundle arguments = new Bundle();
        DocumentListFragment fragment = new DocumentListFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_4, fragment)
                .commit();
    }

    private void shiftViewsLeft() {
    	mViewsShifted = true;
    	mFrame1.setVisibility(View.GONE);
    	mFrame4.setVisibility(View.VISIBLE);
    }

    private void shiftViewsRight() {
    	mViewsShifted = false;
    	mFrame4.setVisibility(View.GONE);
    	mFrame1.setVisibility(View.VISIBLE);
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
    
    @Override
    public void onAddConnection() {
    	addConnection();
    }

    private void addConnection() {
        DialogFragment fragment = ConnectionSetupDialogFragment.create(0, this);
        fragment.show(getSupportFragmentManager(), null);
    }

	@Override
	public void onCollectionItemSelected(long id) {
		shiftViewsLeft();
		loadDocumentListPane();
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
	public void onConnectionSaved(long id) {
		ConnectionListFragment fragment = (ConnectionListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_connection_list);
        fragment.refreshList(id);
        invalidateOptionsMenu();
        
        if (mTwoPane)
        	loadDetailsPane(id);
	}
}
