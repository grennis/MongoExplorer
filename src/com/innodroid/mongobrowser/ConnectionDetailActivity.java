package com.innodroid.mongobrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.Window;

public class ConnectionDetailActivity extends FragmentActivity implements ConnectionDetailFragment.Callbacks {

    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_detail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            ConnectionDetailFragment fragment = new ConnectionDetailFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.connection_detail_container, fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home:
    			finish();
                return true;
    		default:
               	break;
    	}

        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onConnected() {
		Intent activ = new Intent(ConnectionDetailActivity.this, CollectionListActivity.class);
		startActivity(activ);
		finish();
	}

	@Override
	public void onConnectionDeleted() {
		finish();
	}
}
