package com.innodroid.mongobrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;

public class ConnectionDetailActivity extends AppCompatActivity implements ConnectionDetailFragment.Callbacks {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_pane);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            ConnectionDetailFragment fragment = new ConnectionDetailFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.root_content, fragment).commit();
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
	public void onConnected(long connectionId) {
		Intent activ = new Intent(ConnectionDetailActivity.this, CollectionListActivity.class);
		activ.putExtra(Constants.ARG_CONNECTION_ID, connectionId);
		startActivity(activ);
		finish();
	}

	@Override
	public void onConnectionDeleted() {
		finish();
	}
}
