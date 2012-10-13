package com.innodroid.mongobrowser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class DocumentEditActivity extends FragmentActivity implements DocumentEditFragment.Callbacks {
	private int mPosition;
	
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_edit);
        
        mPosition = getIntent().getIntExtra(Constants.ARG_POSITION, -1);
        
        if (mPosition >= 0)
        	setTitle(R.string.edit_document);
        else
        	setTitle(R.string.add_document);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
        	DocumentEditFragment fragment = new DocumentEditFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.document_edit_container, fragment).commit();
        }
    }

    @Override
    public void onDocumentSaved(int position, String content) {
    	Intent data = new Intent();
    	data.putExtra(Constants.ARG_POSITION, position);
    	data.putExtra(Constants.ARG_DOCUMENT_CONTENT, content);
    	setResult(RESULT_OK, data);
    	finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, DocumentDetailActivity.class));
                return true;
    		default:
               	break;
    	}

        return super.onOptionsItemSelected(item);
    }
}
