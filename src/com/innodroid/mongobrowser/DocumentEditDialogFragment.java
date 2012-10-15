package com.innodroid.mongobrowser;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.innodroid.mongo.MongoHelper;
import com.innodroid.mongobrowser.CollectionEditDialogFragment.Callbacks;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;

public class DocumentEditDialogFragment extends DialogFragment {

	private EditText mContentEdit;
	private String mContent;
	private String mCollectionName;
	private Callbacks mCallbacks;

    public interface Callbacks {
    	void onDocumentSaved(int position, String content);
    }

    static DocumentEditDialogFragment create(String collectionName, int position, String content, Callbacks callbacks) {
    	DocumentEditDialogFragment fragment = new DocumentEditDialogFragment();
    	Bundle args = new Bundle();
    	args.putInt(Constants.ARG_POSITION, position);
    	args.putString(Constants.ARG_DOCUMENT_CONTENT, content);
    	args.putString(Constants.ARG_COLLECTION_NAME, collectionName);
    	fragment.setArguments(args);
    	fragment.mCallbacks = callbacks;
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_document_edit, null);

    	mContent = getArguments().getString(Constants.ARG_DOCUMENT_CONTENT);
    	mCollectionName = getArguments().getString(Constants.ARG_COLLECTION_NAME);
    	mContentEdit = (EditText)view.findViewById(R.id.document_edit_content);
    	mContentEdit.setText(mContent);
    	
    	return UiUtils.buildAlertDialog(view, android.R.drawable.ic_menu_edit, mCollectionName, true, new UiUtils.AlertDialogCallbacks() {			
			@Override
			public boolean onOK() {
				save();
				return true;
			}
		});    	
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setHasOptionsMenu(true);    	
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.document_edit_menu, menu);
    }    

    public void save() {
		new SaveDocumentTask().execute(mContentEdit.getText().toString());
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
			int position = getArguments().getInt(Constants.ARG_POSITION);
			mCallbacks.onDocumentSaved(position, result);
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
