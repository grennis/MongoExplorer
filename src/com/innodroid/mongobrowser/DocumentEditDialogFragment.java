package com.innodroid.mongobrowser;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.innodroid.mongo.MongoHelper;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;

public class DocumentEditDialogFragment extends DialogFragment {

	private EditText mContentEdit;
	private String mContent;
	private String mCollectionName;
	private static Callbacks mCallbacks;

    public interface Callbacks {
    	void onDocumentCreated(String content);
    	void onDocumentUpdated(String content);
    }

    static DocumentEditDialogFragment create(String collectionName, boolean isNew, String content, Callbacks callbacks) {
    	DocumentEditDialogFragment fragment = new DocumentEditDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(Constants.ARG_DOCUMENT_CONTENT, content);
    	args.putString(Constants.ARG_COLLECTION_NAME, collectionName);
    	args.putBoolean(Constants.ARG_IS_NEW, isNew);
    	fragment.setArguments(args);
    	DocumentEditDialogFragment.mCallbacks = callbacks;
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_document_edit, null);

    	mContent = getArguments().getString(Constants.ARG_DOCUMENT_CONTENT);
    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME);
    	mContentEdit = (EditText)view.findViewById(R.id.document_edit_content);
    	mContentEdit.setText(mContent);
    	
    	return UiUtils.buildAlertDialog(view, android.R.drawable.ic_menu_edit, mCollectionName, true, 0, new UiUtils.AlertDialogCallbacks() {			
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
    
    @Override
    public void onDestroyView() {
    	// http://stackoverflow.com/questions/8235080/fragments-dialogfragment-and-screen-rotation
    	if (getDialog() != null && getRetainInstance())
    		getDialog().setDismissMessage(null);
    	super.onDestroyView();
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
				mCallbacks.onDocumentCreated(result);
			else
				mCallbacks.onDocumentUpdated(result);
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
