package com.innodroid.mongobrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;

public class CollectionListActivity extends AppCompatActivity implements CollectionListFragment.Callbacks {
	public static final int REQUEST_DOCUMENT_LIST = 101;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_collection_list);
        setContentView(R.layout.activity_single_pane);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
        	Bundle args = new Bundle();
	        CollectionListFragment fragment = new CollectionListFragment();
	        args.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, false);
	        fragment.setArguments(args);
	        getSupportFragmentManager().beginTransaction()
	                .replace(R.id.root_content, fragment)
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
    public void onCollectionItemSelected(long connectionId, String name) {
        Intent intent = new Intent(this, DocumentListActivity.class);
        intent.putExtra(Constants.ARG_COLLECTION_NAME, name);
        intent.putExtra(Constants.ARG_CONNECTION_ID, connectionId);
        startActivityForResult(intent, REQUEST_DOCUMENT_LIST);
    }
	
	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		super.onActivityResult(request, result, data);

		if (request == REQUEST_DOCUMENT_LIST) {
			// The collection may have been renamed
	        CollectionListFragment fragment = (CollectionListFragment)getSupportFragmentManager().findFragmentById(R.id.root_content);
	        fragment.reloadList();			
		}
	}
}


