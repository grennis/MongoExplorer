package com.innodroid.mongobrowser.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.util.MongoHelper;
import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;

import butterknife.Bind;

public class DocumentEditDialogFragment extends BaseDialogFragment {
	@Bind(R.id.document_edit_content) EditText mContentEdit;

	private String mContent;
	private String mCollectionName;

	public DocumentEditDialogFragment() {
		super();
	}
	
    static DocumentEditDialogFragment create(String collectionName, boolean isNew, String content) {
    	DocumentEditDialogFragment fragment = new DocumentEditDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(Constants.ARG_DOCUMENT_CONTENT, content);
    	args.putString(Constants.ARG_COLLECTION_NAME, collectionName);
    	args.putBoolean(Constants.ARG_IS_NEW, isNew);
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = super.onCreateDialog(R.layout.fragment_document_edit);

    	mContent = getArguments().getString(Constants.ARG_DOCUMENT_CONTENT);
    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME);
    	mContentEdit.setText(mContent);
    	
    	return UiUtils.buildAlertDialog(view, R.drawable.ic_mode_edit_black, mCollectionName, true, 0, new UiUtils.AlertDialogCallbacks() {
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
			return MongoHelper.saveDocument(mCollectionName, content[0]);
		}
		
		@Override
		protected void safeOnPostExecute(String result) {
			if (getArguments().getBoolean(Constants.ARG_IS_NEW))
				Events.postDocumentCreated(result);
			else
				Events.postDocumentEdited(result);
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
