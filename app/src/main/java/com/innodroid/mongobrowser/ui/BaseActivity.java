package com.innodroid.mongobrowser.ui;

import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;
import com.innodroid.mongobrowser.util.SafeAsyncTask;

import de.greenrobot.event.EventBus;

public abstract class BaseActivity extends AppCompatActivity {
	private static final String STATE_COLLECTION_NAME = "collname";
	
	protected String mCollectionName;
	protected FrameLayout mFrame1;
	protected static boolean mHavePromptedToAddConnection = false;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResource());

        mFrame1 = (FrameLayout)findViewById(R.id.frame_1);

        if (savedInstanceState == null) {
        	loadConnectionListPane();
        } else {
        	mCollectionName = savedInstanceState.getString(STATE_COLLECTION_NAME);
        }

    	if (!mHavePromptedToAddConnection) {
    		mHavePromptedToAddConnection = true;
    		new AddConnectionIfNoneExistTask().execute();
    	}
    }

	@Override
	protected void onStart() {
		super.onStart();

		EventBus.getDefault().register(this);
	}

	@Override
	protected void onStop() {
		super.onStop();

		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_COLLECTION_NAME, mCollectionName);
	}

	protected abstract int getLayoutResource();
	protected abstract void loadConnectionListPane();
	protected abstract void loadConnectionDetailsPane(long id);
	protected abstract void loadCollectionListPane(long connectionId);
	protected abstract void loadDocumentListPane(long connectionId, String collection);
	protected abstract void loadDocumentDetailsPane(String content);
	protected abstract void hideDocumentDetailPane();

	public void onEvent(Events.ConnectionSelected e) {
		loadConnectionDetailsPane(e.ConnectionId);
	}

	private void showAddConnection() {
		DialogFragment fragment = ConnectionEditDialogFragment.newInstance(0);
		fragment.show(getSupportFragmentManager(), null);
	}

    public void onEvent(Events.AddConnection e) {
		showAddConnection();
    }

	public void onEvent(Events.AddDocument e) {
		DocumentEditDialogFragment fragment = DocumentEditDialogFragment.newInstance(mCollectionName, true, Constants.NEW_DOCUMENT_CONTENT_PADDED);
		fragment.show(getSupportFragmentManager(), null);
	}

	public void onEvent(Events.CollectionSelected e) {
		if (e.CollectionName != null) {
			mCollectionName = e.CollectionName;
			loadDocumentListPane(e.ConnectionId, e.CollectionName);
		}
	}

	public void onEvent(Events.DocumentClicked e) {
		loadDocumentDetailsPane(e.Content);
	}

	public void onEvent(Events.Connected e) {
		loadCollectionListPane(e.ConnectionId);
	}

	public void onEvent(Events.ConnectionDeleted e) {
        getSupportFragmentManager().beginTransaction()
	        .remove(getSupportFragmentManager().findFragmentById(R.id.frame_2))
	        .commit();        
	}

	public void onEvent(Events.ConnectionAdded e) {
		reloadConnectionListAndSelect(e.ConnectionId);
	}

	public void onEvent(Events.ConnectionUpdated e) {
		reloadConnectionListAndSelect(e.ConnectionId);
	}

	public void onEvent(Events.ShowSettings e) {
		DialogFragment fragment = SettingsFragment.newInstance();
		fragment.show(getSupportFragmentManager(), null);
	}

	protected void reloadConnectionListAndSelect(long id) {
		if (isFinishing())
			return;
		
		ConnectionListFragment fragment = (ConnectionListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);
		
		if (fragment != null)
			fragment.reloadAndSelect(id);
	}

	public void onEvent(Events.DocumentDeleted e) {
		hideDocumentDetailPane();
		getSupportFragmentManager().executePendingTransactions();
	}

	public void onEvent(Events.EditDocument e) {
		DocumentEditDialogFragment fragment = DocumentEditDialogFragment.newInstance(mCollectionName, false, e.Content);
		fragment.show(getSupportFragmentManager(), null);
	}

    private class AddConnectionIfNoneExistTask extends SafeAsyncTask<Void, Void, Boolean> {
		public AddConnectionIfNoneExistTask() {
			super(BaseActivity.this);
		}

		@Override
		protected Boolean safeDoInBackground(Void... arg0) {
			return new MongoBrowserProviderHelper(getContentResolver()).getConnectionCount() == 0;
		}

		@Override
		protected void safeOnPostExecute(Boolean res) {
			if (res) {
				showAddConnection();
			}
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Check Connections";
		}
    }
}

