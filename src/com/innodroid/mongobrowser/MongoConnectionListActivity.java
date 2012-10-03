package com.innodroid.mongobrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MongoConnectionListActivity extends FragmentActivity implements MongoConnectionListFragment.Callbacks {

    private boolean mTwoPane;

    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mongoconnection_list);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
        	getActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.mongoconnection_detail_container) != null) {
            mTwoPane = true;
            ((MongoConnectionListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mongoconnection_list))
                    .setActivateOnItemClick(true);
        }
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
            	Toast.makeText(this, "Add Item", Toast.LENGTH_LONG).show();
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
            arguments.putLong(MongoConnectionDetailFragment.ARG_ITEM_ID, id);
            MongoConnectionDetailFragment fragment = new MongoConnectionDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mongoconnection_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, MongoConnectionDetailActivity.class);
            detailIntent.putExtra(MongoConnectionDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
