package com.innodroid.mongobrowser;

import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ConnectionListActivity extends FragmentActivity implements ConnectionListFragment.Callbacks {
    private boolean mTwoPane;

    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_connection_list);
        setContentView(R.layout.activity_connection_list);

        if (findViewById(R.id.connection_detail_container) != null) {
            mTwoPane = true;
            ((ConnectionListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mongoconnection_list))
                    .setActivateOnItemClick(true);
        }
        
        new AddConnectionIfNoneExistTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.connection_list_menu, menu);
        return true;
    }
        
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connection_list_menu_add:
            	editConnection();
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
            Bundle arguments = new Bundle();
            arguments.putLong(ConnectionDetailFragment.ARG_CONNECTION_ID, id);
            ConnectionDetailFragment fragment = new ConnectionDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.connection_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, ConnectionDetailActivity.class);
            detailIntent.putExtra(ConnectionDetailFragment.ARG_CONNECTION_ID, id);
            startActivity(detailIntent);
        }
    }
	
    private void editConnection() {
        DialogFragment fragment = ConnectionSetupDialogFragment.create(0);
        fragment.show(getSupportFragmentManager(), null);
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
				editConnection();
		}
    }
}
