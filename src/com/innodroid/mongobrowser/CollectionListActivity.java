package com.innodroid.mongobrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.Window;

public class CollectionListActivity extends FragmentActivity implements CollectionListFragment.Callbacks {	
	public static final int REQUEST_DOCUMENT_LIST = 101;
	
	@SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_collection_list);
        setContentView(R.layout.activity_collection_list);        

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
        	Bundle args = new Bundle();
	        CollectionListFragment fragment = new CollectionListFragment();
	        args.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, false);
	        fragment.setArguments(args);
	        getSupportFragmentManager().beginTransaction()
	                .replace(R.id.collection_list, fragment)
	                .commit();	        
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
    public void onCollectionItemSelected(String name) {
        Intent intent = new Intent(this, DocumentListActivity.class);
        intent.putExtra(Constants.ARG_COLLECTION_NAME, name);
        startActivityForResult(intent, REQUEST_DOCUMENT_LIST);
    }
	
	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		super.onActivityResult(request, result, data);

		if (request == REQUEST_DOCUMENT_LIST) {
			// The collection may have been renamed
	        CollectionListFragment fragment = (CollectionListFragment)getSupportFragmentManager().findFragmentById(R.id.collection_list);
	        fragment.reloadList();			
		}
	}
}


