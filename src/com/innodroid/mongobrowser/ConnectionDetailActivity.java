package com.innodroid.mongobrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
    	long id = getIntent().getLongExtra(ConnectionDetailFragment.ARG_CONNECTION_ID, 0);

    	switch (item.getItemId()) {
    		case R.id.connection_detail_menu_edit:
    			Utils.editConnection(this, id);
    			return true;
    		case R.id.connection_detail_menu_delete:
    			Utils.deleteConnection(this, id, true);
    			return true;
    		case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, ConnectionListActivity.class));
                return true;
    		default:
               	break;
    	}

        return super.onOptionsItemSelected(item);
    }
}
