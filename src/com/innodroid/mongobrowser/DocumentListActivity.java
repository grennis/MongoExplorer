package com.innodroid.mongobrowser;

import com.innodroid.mongo.MongoHelper;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class DocumentListActivity extends FragmentActivity implements DocumentListFragment.Callbacks {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String name = getIntent().getStringExtra(Constants.ARG_COLLECTION_NAME);
        setTitle(name);
        setContentView(R.layout.activity_document_list);
        
        if (savedInstanceState == null) {
	        DocumentListFragment fragment = new DocumentListFragment();
	        fragment.setArguments(getIntent().getExtras());
	        getSupportFragmentManager().beginTransaction().add(R.id.document_list, fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, CollectionListActivity.class));
                return true;
    		default:
               	break;
    	}

        return super.onOptionsItemSelected(item);
    }
    
	@Override
    public void onDocumentItemSelected(long id) {
    }

	@Override
	public void onCollectionEdited(String name) {
		setTitle(name);
	}
}
