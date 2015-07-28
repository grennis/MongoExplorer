package com.innodroid.mongobrowser.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.innodroid.mongobrowser.util.MongoHelper;
import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.util.SafeAsyncTask;

import butterknife.Bind;

public class DocumentEditFragment extends BaseFragment {
	@Bind(R.id.document_edit_content) EditText mContentEdit;

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

    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME);

    	setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = super.onCreateView(R.layout.fragment_document_edit, inflater, container, savedInstanceState);

    	String json = getArguments().getString(Constants.ARG_DOCUMENT_CONTENT);
    	mContentEdit.setText(json);
		getActivity().setProgressBarIndeterminateVisibility(false);

        return view;
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
