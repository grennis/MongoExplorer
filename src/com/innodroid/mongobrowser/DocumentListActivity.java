package com.innodroid.mongobrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public class DocumentListActivity extends FragmentActivity implements DocumentListFragment.Callbacks {
	private static final int REQUEST_EDIT_DOCUMENT = 101;
	private static final int REQUEST_VIEW_DOCUMENT = 102;
	
	private String mCollectionName;
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
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
		intent.putExtra(Constants.ARG_POSITION, -1);
		intent.putExtra(Constants.ARG_DOCUMENT_CONTENT, Constants.NEW_DOCUMENT_CONTENT);
		intent.putExtra(Constants.ARG_COLLECTION_NAME, mCollectionName);
		startActivityForResult(intent, REQUEST_EDIT_DOCUMENT);		
	}

	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		if ((request == REQUEST_EDIT_DOCUMENT || request == REQUEST_VIEW_DOCUMENT) && result == RESULT_OK) {
			DocumentListFragment fragment = (DocumentListFragment)getSupportFragmentManager().findFragmentById(R.id.document_list);
			fragment.onDocumentSaved(data.getIntExtra(Constants.ARG_POSITION, -1), data.getStringExtra(Constants.ARG_DOCUMENT_CONTENT));
		} else {
			super.onActivityResult(request, result, data);
		}
	}
	
	@Override
    public void onDocumentItemSelected(int position, String content) {
		Intent intent = new Intent(this, DocumentDetailActivity.class);
		intent.putExtra(Constants.ARG_POSITION, position);
		intent.putExtra(Constants.ARG_DOCUMENT_CONTENT, content);
		intent.putExtra(Constants.ARG_COLLECTION_NAME, mCollectionName);
		startActivityForResult(intent, REQUEST_VIEW_DOCUMENT);
    }

	@Override
	public void onCollectionEdited(int position, String name) {
		setTitle(name);
	}

	@Override
	public void onCollectionDropped(int position, String name) {
		finish();
	}
}
