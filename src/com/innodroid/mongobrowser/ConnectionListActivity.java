package com.innodroid.mongobrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;

public class ConnectionListActivity extends FragmentActivity implements ConnectionListFragment.Callbacks {
    private boolean mTwoPane;
    private long mSelectedID;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_connection_list);
        setContentView(R.layout.activity_connection_list);

        if (findViewById(R.id.connection_detail_container) != null) {
            mTwoPane = true;
            ((ConnectionListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.connection_list))
                    .setActivateOnItemClick(true);
        }
        
        LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshReceiver, new IntentFilter(Constants.MessageConnectionItemChanged));

        new AddConnectionIfNoneExistTask().execute();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (mTwoPane) {
    		boolean enable = mSelectedID != 0;
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
    			Utils.editConnection(this, mSelectedID);
    			return true;
    		case R.id.connection_detail_menu_delete:
    			Utils.deleteConnection(this, mSelectedID, false);
    			return true;
            case R.id.connection_list_menu_configure:
            	Toast.makeText(this, "Configure", Toast.LENGTH_LONG).show();
                return true;
        }

    	return super.onOptionsItemSelected(item);
    }

	@Override
    public void onItemSelected(long id) {
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
    	mSelectedID = id;
        Bundle arguments = new Bundle();
        arguments.putLong(ConnectionDetailFragment.ARG_CONNECTION_ID, id);
        ConnectionDetailFragment fragment = new ConnectionDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.connection_detail_container, fragment)
                .commit();
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
				mSelectedID = intent.getLongExtra(Constants.MessageItemID, 0);
				loadDetailsPane(mSelectedID);
			}
			invalidateOptionsMenu();
		}
	};
}
