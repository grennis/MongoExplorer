package com.innodroid.mongobrowser.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.util.MongoHelper;
import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.util.JsonUtils;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;
import com.innodroid.mongobrowser.util.UiUtils.ConfirmCallbacks;

import butterknife.Bind;

public class DocumentDetailFragment extends BaseFragment {
	@Bind(R.id.document_detail_content) TextView mContentText;

	private String mCollectionName;
	private String mRawText;
	private String mFormattedText;

    public DocumentDetailFragment() {
    }

	@NonNull
	public static DocumentDetailFragment newInstance(String content, String collectionName) {
		Bundle arguments = new Bundle();
		DocumentDetailFragment fragment = new DocumentDetailFragment();
		arguments.putString(Constants.ARG_COLLECTION_NAME, collectionName);
		arguments.putString(Constants.ARG_DOCUMENT_CONTENT, content);
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);

    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(R.layout.fragment_document_detail, inflater, container, savedInstanceState);

		updateContent(getArguments().getString(Constants.ARG_DOCUMENT_CONTENT));
		getActivity().setProgressBarIndeterminateVisibility(false);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.document_detail_menu, menu);
    }    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_document_detail_edit:
    			if (mRawText != null)
    				Events.postEditDocument(mFormattedText);
    			return true;
    		case R.id.menu_document_detail_delete:
    			if (mRawText != null)
    				deleteDocument();
    			return true;
    		default:
    	    	return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	menu.findItem(R.id.menu_document_detail_edit).setEnabled(mRawText != null);
    	menu.findItem(R.id.menu_document_detail_delete).setEnabled(mRawText != null);
    }

	public void onEvent(Events.DocumentEdited e) {
		updateContent(e.Content);
	}

    private void deleteDocument() {
		UiUtils.confirm(getActivity(), R.string.confirm_delete_document, new ConfirmCallbacks() {					
			@Override
			public boolean onConfirm() {
				new DeleteDocumentTask().execute();
				return true;
			}
		});
    }
    
    public void updateContent(String json) {
    	mRawText = json;
    	
    	if (mRawText == null) {
    		mContentText.setText("");
    		mFormattedText = null;
    	} else {
    		mFormattedText = JsonUtils.prettyPrint(json);   
    		//mContentText.setText(mFormattedText);
    		mContentText.setText(JsonUtils.prettyPrint2(json));
    	}
    	
		getActivity().invalidateOptionsMenu();
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
			Events.postDocumentDeleted();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Delete";
		}		
    }
}


