package com.innodroid.mongobrowser.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.data.MongoData;
import com.innodroid.mongobrowser.util.JsonUtils;
import com.innodroid.mongobrowser.util.MongoHelper;
import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;

import butterknife.Bind;

public class DocumentEditDialogFragment extends BaseDialogFragment {
	@Bind(R.id.document_edit_content) EditText mContentEdit;

	private int mDocumentIndex;
	private int mCollectionIndex;

	public DocumentEditDialogFragment() {
		super();
	}
	
    public static DocumentEditDialogFragment newInstance(int collectionIndex, int documentIndex) {
    	DocumentEditDialogFragment fragment = new DocumentEditDialogFragment();
    	Bundle args = new Bundle();
		args.putInt(Constants.ARG_COLLECTION_INDEX, collectionIndex);
		args.putInt(Constants.ARG_DOCUMENT_INDEX, documentIndex);
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = super.onCreateDialog(R.layout.fragment_document_edit);

		mDocumentIndex = getArguments().getInt(Constants.ARG_DOCUMENT_INDEX);
    	mCollectionIndex = getArguments().getInt(Constants.ARG_COLLECTION_INDEX);

		if (mDocumentIndex < 0) {
			mContentEdit.setText(Constants.NEW_DOCUMENT_CONTENT_PADDED);
		} else {
			String json = MongoData.Documents.get(mDocumentIndex).Content;
			mContentEdit.setText(JsonUtils.prettyPrint(json));
		}

		String title = MongoData.Collections.get(mCollectionIndex).Name;
    	return UiUtils.buildAlertDialog(view, R.drawable.ic_mode_edit_black, title, true, 0, new UiUtils.AlertDialogCallbacks() {
			@Override
			public boolean onOK() {
				save();
				return true;
			}

			@Override
			public boolean onNeutralButton() {
				return false;
			}
		});    	
    }
    
    public void save() {
    	String doc = mContentEdit.getText().toString();
    	new SaveDocumentTask().execute(doc);
    }

    public class SaveDocumentTask extends SafeAsyncTask<String, Void, String> {
    	public SaveDocumentTask() {
			super(getActivity());
		}

		@Override
		protected String safeDoInBackground(String... content) {
			String collection = MongoData.Collections.get(mCollectionIndex).Name;
			return MongoHelper.saveDocument(collection, content[0]);
		}
		
		@Override
		protected void safeOnPostExecute(String result) {
			if (mDocumentIndex < 0) {
				Events.postDocumentCreated(result);
			} else {
				MongoData.Documents.get(mDocumentIndex).Content = result;
				Events.postDocumentEdited(mDocumentIndex);
			}
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
