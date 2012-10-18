package com.innodroid.mongobrowser;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.innodroid.mongobrowser.util.UiUtils;

public class QueryEditDialogFragment extends DialogFragment {

	private EditText mContentEdit;
	private String mContent;
	private static Callbacks mCallbacks;

    public interface Callbacks {
    	void onQueryUpdated(String query);
    	void onQueryCleared();
    }

    static QueryEditDialogFragment create(String content, Callbacks callbacks) {
    	QueryEditDialogFragment fragment = new QueryEditDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(Constants.ARG_DOCUMENT_CONTENT, content);
    	fragment.setArguments(args);
    	QueryEditDialogFragment.mCallbacks = callbacks;
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_query_edit, null);

    	mContent = getArguments().getString(Constants.ARG_DOCUMENT_CONTENT);
    	mContentEdit = (EditText)view.findViewById(R.id.query_edit_content);
    	mContentEdit.setText(mContent);
    	
    	return UiUtils.buildAlertDialog(view, android.R.drawable.ic_menu_edit, "Edit Query", true, R.string.clear, new UiUtils.AlertDialogCallbacks() {			
			@Override
			public boolean onOK() {
				mCallbacks.onQueryUpdated(mContentEdit.getText().toString());
				return true;
			}

			@Override
			public boolean onNeutralButton() {
				mCallbacks.onQueryCleared();
				return true;
			}
		});    
    }
}
