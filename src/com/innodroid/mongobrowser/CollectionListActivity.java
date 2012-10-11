package com.innodroid.mongobrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class CollectionListActivity extends FragmentActivity implements CollectionListFragment.Callbacks {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_collection_list);
        setContentView(R.layout.activity_collection_list);        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, ConnectionListActivity.class));
                return true;
    		default:
               	break;
    	}

        return super.onOptionsItemSelected(item);
    }
    
	@Override
    public void onCollectionItemSelected(String name) {
        Intent intent = new Intent(this, DocumentListActivity.class);
        intent.putExtra(Constants.ARG_COLLECTION_NAME, name);
        startActivity(intent);
    }
}


