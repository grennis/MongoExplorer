package com.innodroid.mongobrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;

public class ConnectionListActivity extends FragmentActivity implements ConnectionListFragment.Callbacks, CollectionListFragment.Callbacks {
    private boolean mTwoPane;
    private boolean mViewsShifted;
    private FrameLayout mFrame1;
    private FrameLayout mFrame2;
    private FrameLayout mFrame3;
    private FrameLayout mFrame4;
    private FrameLayout mFrame5;
    private long mSelectedConnectionID;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_connection_list);
        setContentView(R.layout.activity_connection_list);

        mFrame1 = (FrameLayout)findViewById(R.id.frame_1);
        mFrame2 = (FrameLayout)findViewById(R.id.frame_2);
        mFrame3 = (FrameLayout)findViewById(R.id.frame_3);
        mFrame4 = (FrameLayout)findViewById(R.id.frame_4);
        mFrame5 = (FrameLayout)findViewById(R.id.frame_5);
        
        if (mFrame1 != null) {
            mTwoPane = true;
            ((ConnectionListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_connection_list))
                    .setActivateOnItemClick(true);
        }
        
        LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshReceiver, new IntentFilter(Constants.MessageConnectionItemChanged));
        LocalBroadcastManager.getInstance(this).registerReceiver(mConnectedReceiver, new IntentFilter(Constants.MessageConnected));

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
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (mTwoPane) {
    		boolean enable = mSelectedConnectionID != 0;
    		menu.getItem(1).setEnabled(enable);
    		menu.getItem(2).setEnabled(enable);
    	}
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.connection_list_menu, menu);
        
    	if (!mTwoPane) {
    		menu.getItem(1).setVisible(false);
    		menu.getItem(2).setVisible(false);
    	}
    	
        return true;
    }
        
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connection_list_menu_add:
            	Utils.addConnection(this);
                return true;
    		case R.id.connection_detail_menu_edit:
    			Utils.editConnection(this, mSelectedConnectionID);
    			return true;
    		case R.id.connection_detail_menu_delete:
    			Utils.deleteConnection(this, mSelectedConnectionID, false);
    			return true;
            case R.id.connection_list_menu_configure:
            	Intent intent = new Intent(this, PreferencesActivity.class);
            	startActivity(intent);
                return true;
        }

    	return super.onOptionsItemSelected(item);
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
    	mSelectedConnectionID = id;
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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_3, fragment)
                .commit();
    }

    private void shiftViewsLeft() {
    	mViewsShifted = true;
    	mFrame1.setVisibility(View.GONE);
    	mFrame2.setVisibility(View.GONE);
    	mFrame4.setVisibility(View.VISIBLE);
    	mFrame5.setVisibility(View.VISIBLE);
    }

    private void shiftViewsRight() {
    	mViewsShifted = false;
    	mFrame4.setVisibility(View.GONE);
    	mFrame5.setVisibility(View.GONE);
    	mFrame1.setVisibility(View.VISIBLE);
    	mFrame2.setVisibility(View.VISIBLE);
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
				Utils.addConnection(ConnectionListActivity.this);
		}
    }
    
	private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (mTwoPane) {
				mSelectedConnectionID = intent.getLongExtra(Constants.MessageItemID, 0);
				loadDetailsPane(mSelectedConnectionID);
			}
			invalidateOptionsMenu();
		}
	};
	
	private BroadcastReceiver mConnectedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mTwoPane) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						loadCollectionListPane();
					}					
				});
			}
	    }
	};

	@Override
	public void onCollectionItemSelected(long id) {
		shiftViewsLeft();
	}
}
