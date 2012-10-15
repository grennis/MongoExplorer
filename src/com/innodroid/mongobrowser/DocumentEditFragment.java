package com.innodroid.mongobrowser;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.innodroid.mongo.MongoHelper;
import com.innodroid.mongobrowser.util.SafeAsyncTask;

public class DocumentEditFragment extends Fragment {

	private EditText mContentEdit;
	private String mCollectionName;
	private Callbacks mCallbacks;

    public interface Callbacks {
    	void onDocumentSaved(String content);
    }

    public DocumentEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setHasOptionsMenu(true);    	
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_document_edit, container, false);
    	
    	mContentEdit = (EditText) view.findViewById(R.id.document_edit_content);
    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME); 
    	
    	String json = getArguments().getString(Constants.ARG_DOCUMENT_CONTENT);
    	mContentEdit.setText(json);
    	
        return view;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        
        //getLoaderManager().initLoader(0, getArguments(), this);
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	
    	mCallbacks = (Callbacks)activity;
    }
    
    @Override
    public void onDetach() {
    	super.onDetach();
    	
    	mCallbacks = null;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.document_edit_menu, menu);
    }    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_document_edit_save:
    			new SaveDocumentTask().execute(mContentEdit.getText().toString());
    			return true;
    		default:
    	    	return super.onOptionsItemSelected(item);    		
    	}
    }
    
    public class SaveDocumentTask extends SafeAsyncTask<String, Void, String> {
    	public SaveDocumentTask() {
			super(getActivity());
		}

		@Override
		protected String safeDoInBackground(String... content) {
			return MongoHelper.saveDocument(mCollectionName, content[0]);
		}
		
		@Override
		protected void safeOnPostExecute(String result) {			
			mCallbacks.onDocumentSaved(result);
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Save";
		}
		
		@Override
		protected String getProgressMessage() {
			return "Saving";
		}
    }
}
