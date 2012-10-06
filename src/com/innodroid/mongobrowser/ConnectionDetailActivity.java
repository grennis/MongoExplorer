package com.innodroid.mongobrowser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.innodroid.mongobrowser.data.MongoBrowserProvider;

public class ConnectionDetailActivity extends FragmentActivity {

    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_detail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putLong(ConnectionDetailFragment.ARG_CONNECTION_ID, getIntent().getLongExtra(ConnectionDetailFragment.ARG_CONNECTION_ID, 0));
            ConnectionDetailFragment fragment = new ConnectionDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().add(R.id.connection_detail_container, fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.connection_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.connection_detail_menu_edit:
    			editConnection();
    			return true;
    		case R.id.connection_detail_menu_delete:
    			deleteConnection();
    			return true;
    		case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, ConnectionListActivity.class));
                return true;
    		default:
               	break;
    	}

        return super.onOptionsItemSelected(item);
    }
    
    private void editConnection() {
    	long id = getIntent().getLongExtra(ConnectionDetailFragment.ARG_CONNECTION_ID, 0);
        DialogFragment fragment = ConnectionSetupDialogFragment.create(id);
        fragment.show(getSupportFragmentManager(), null);
    }
    
    private void deleteConnection() {
        new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_menu_delete)
	        .setMessage(R.string.confirm_delete_connection)
	        .setTitle(R.string.confirm_delete_title)
	        .setCancelable(true)
	        .setPositiveButton(android.R.string.ok,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	doDeleteConnection();
	                }
	            }
	        )
	        .setNegativeButton(android.R.string.cancel,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	//
	                }
	            }
	        )
	        .create().show();
    }
    
    private void doDeleteConnection() {
    	long id = getIntent().getLongExtra(ConnectionDetailFragment.ARG_CONNECTION_ID, 0);
    	new DeleteConnectionTask(id).execute();
    }
    
    private class DeleteConnectionTask extends AsyncTask<Void, Void, Boolean> {
    	private long mID;
    	
    	public DeleteConnectionTask(long id) {
    		mID = id;
    	}
    	
		@Override
		protected Boolean doInBackground(Void... arg0) {
			Uri uri = ContentUris.withAppendedId(MongoBrowserProvider.CONNECTION_URI, mID);
			getContentResolver().delete(uri, null, null);
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
	    	Intent intent = new Intent(Constants.MessageRefreshConnectionList);
	    	LocalBroadcastManager.getInstance(ConnectionDetailActivity.this).sendBroadcast(intent);
			finish();
		}
    }    
}
