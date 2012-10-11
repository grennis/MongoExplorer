package com.innodroid.mongobrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class DocumentListActivity extends FragmentActivity implements DocumentListFragment.Callbacks {
    public static final String EXTRA_COLLECTION_NAME = "coll_name";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String name = getIntent().getStringExtra(DocumentListActivity.EXTRA_COLLECTION_NAME);
        setTitle(name);
        setContentView(R.layout.activity_document_list);
        
        if (savedInstanceState == null) {
	        Bundle args = new Bundle();
	        DocumentListFragment fragment = (DocumentListFragment)getSupportFragmentManager().findFragmentById(R.id.document_list);
	        args.putString(DocumentListFragment.ARG_COLLECTION_NAME, name);   
	        fragment.setArguments(args);
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
