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
import android.widget.TextView;

import com.innodroid.mongo.MongoHelper;
import com.innodroid.mongobrowser.util.JsonUtils;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;
import com.innodroid.mongobrowser.util.UiUtils.AlertDialogCallbacks;

public class DocumentDetailFragment extends Fragment {

	private String mCollectionName;
	private String mRawText;
	private String mFormattedText;
	private TextView mContentText;
	private Callbacks mCallbacks;

    public interface Callbacks {
    	void onEditDocument(String content);
		void onDeleteDocument();
    }

    public DocumentDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);    	
    	
    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_document_detail, container, false);
    	
    	mContentText = (TextView) view.findViewById(R.id.document_detail_content);
    	updateContent(getArguments().getString(Constants.ARG_DOCUMENT_CONTENT));
    	
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
    	inflater.inflate(R.menu.document_detail_menu, menu);
    }    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_document_detail_edit:
    			mCallbacks.onEditDocument(mFormattedText);
    			return true;
    		case R.id.menu_document_detail_delete:
    			deleteDocument();
    			return true;
    		default:
    	    	return super.onOptionsItemSelected(item);
    	}
    }
    
    private void deleteDocument() {
		UiUtils.confirm(getActivity(), R.string.confirm_delete_document, new AlertDialogCallbacks() {					
			@Override
			public boolean onOK() {
				new DeleteDocumentTask().execute();
				return true;
			}
		});
    }
    
    public void updateContent(String json) {
    	mRawText = json;
    	mFormattedText = JsonUtils.prettyPrint(json);    	
    	mContentText.setText(mFormattedText);
    }
    
    private class DeleteDocumentTask extends SafeAsyncTask<Void, Void, Void> {
		public DeleteDocumentTask() {
			super(getActivity());
		}

		@Override
		protected Void safeDoInBackground(Void... params) throws Exception {
			MongoHelper.deleteDocument(mCollectionName, mRawText);
			return null;
		}

		@Override
		protected void safeOnPostExecute(Void result) {
			mCallbacks.onDeleteDocument();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Delete";
		}		
    }
}


