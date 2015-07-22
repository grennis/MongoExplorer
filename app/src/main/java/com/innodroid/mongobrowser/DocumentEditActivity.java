package com.innodroid.mongobrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;

public class DocumentEditActivity extends AppCompatActivity implements DocumentEditFragment.Callbacks {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_edit);
        
    	setTitle(getIntent().getStringExtra(Constants.ARG_COLLECTION_NAME));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
