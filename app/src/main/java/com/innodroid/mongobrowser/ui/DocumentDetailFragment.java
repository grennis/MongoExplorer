package com.innodroid.mongobrowser.ui;

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
import com.innodroid.mongobrowser.data.MongoData;
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

	private String mRawText;

    public DocumentDetailFragment() {
    }

	@NonNull
	public static DocumentDetailFragment newInstance(int collectionIndex, int documentIndex) {
		Bundle arguments = new Bundle();
		DocumentDetailFragment fragment = new DocumentDetailFragment();
		arguments.putInt(Constants.ARG_COLLECTION_INDEX, collectionIndex);
		arguments.putInt(Constants.ARG_DOCUMENT_INDEX, documentIndex);
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public int getTitleText() {
		return R.string.document;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(R.layout.fragment_document_detail, inflater, container, savedInstanceState);

		updateContent();

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
    				Events.postEditDocument(getArguments().getInt(Constants.ARG_DOCUMENT_INDEX));
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
		updateContent();
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
    
    public void updateContent() {
		int index = getArguments().getInt(Constants.ARG_DOCUMENT_INDEX);
		String json = MongoData.Documents.get(index).Content;

    	mRawText = json;
    	
    	if (mRawText == null) {
    		mContentText.setText("");
    	} else {
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
			int index = getArguments().getInt(Constants.ARG_COLLECTION_INDEX);
			String collectionName = MongoData.Collections.get(index).Name;
			MongoHelper.deleteDocument(collectionName, mRawText);
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


