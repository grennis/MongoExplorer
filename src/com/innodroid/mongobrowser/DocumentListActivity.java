package com.innodroid.mongobrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.Window;

public class DocumentListActivity extends FragmentActivity implements DocumentListFragment.Callbacks {
	private static final int REQUEST_EDIT_DOCUMENT = 101;
	private static final int REQUEST_VIEW_DOCUMENT = 102;
	
	private String mCollectionName;
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);

        String name = getIntent().getStringExtra(Constants.ARG_COLLECTION_NAME);
        setTitle(name);
        setContentView(R.layout.activity_document_list);
        
        mCollectionName = getIntent().getExtras().getString(Constants.ARG_COLLECTION_NAME);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
        	Bundle args = getIntent().getExtras();
	        DocumentListFragment fragment = new DocumentListFragment();
	        args.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, false);
	        fragment.setArguments(args);
	        getSupportFragmentManager().beginTransaction().add(R.id.document_list, fragment).commit();
        }
        
        setProgressBarIndeterminateVisibility(false);        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home:
    			finish();
                return true;
    		default:
    	        return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
	public void onAddDocument() {
		Intent intent = new Intent(this, DocumentEditActivity.class);
		intent.putExtra(Constants.ARG_DOCUMENT_CONTENT, Constants.NEW_DOCUMENT_CONTENT);
		intent.putExtra(Constants.ARG_COLLECTION_NAME, mCollectionName);
		startActivityForResult(intent, REQUEST_EDIT_DOCUMENT);		
	}

	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		if ((request == REQUEST_EDIT_DOCUMENT || request == REQUEST_VIEW_DOCUMENT) && result == RESULT_OK) {
			String content = data.getStringExtra(Constants.ARG_DOCUMENT_CONTENT);
			DocumentListFragment fragment = (DocumentListFragment)getSupportFragmentManager().findFragmentById(R.id.document_list);
			
			if (content == null)
				fragment.onDocumentDeleted();
			else if (request == REQUEST_EDIT_DOCUMENT)
				fragment.onDocumentCreated(content);
			else 
				fragment.onDocumentUpdated(content);
				
		} else {
			super.onActivityResult(request, result, data);
		}
	}

	@Override
    public void onDocumentItemActivated(String content) {
	}
	
	@Override
    public void onDocumentItemClicked(String content) {
		Intent intent = new Intent(this, DocumentDetailActivity.class);
		intent.putExtra(Constants.ARG_DOCUMENT_CONTENT, content);
		intent.putExtra(Constants.ARG_COLLECTION_NAME, mCollectionName);
		startActivityForResult(intent, REQUEST_VIEW_DOCUMENT);
    }

	@Override
	public void onCollectionEdited(String name) {
		setTitle(name);
	}

	@Override
	public void onCollectionDropped(String name) {
		finish();
	}
}
