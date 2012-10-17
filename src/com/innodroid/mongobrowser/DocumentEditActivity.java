package com.innodroid.mongobrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.Window;

public class DocumentEditActivity extends FragmentActivity implements DocumentEditFragment.Callbacks {
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_edit);
        
    	setTitle(getIntent().getStringExtra(Constants.ARG_COLLECTION_NAME));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
        	DocumentEditFragment fragment = new DocumentEditFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.document_edit_container, fragment).commit();
        }
    }

    @Override
    public void onDocumentSaved(String content) {
    	Intent data = new Intent();
    	data.putExtra(Constants.ARG_DOCUMENT_CONTENT, content);
    	setResult(RESULT_OK, data);
    	finish();
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
}
