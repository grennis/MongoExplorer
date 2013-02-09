package com.innodroid.mongobrowser;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.innodroid.mongobrowser.util.UiUtils;

public class QueryEditTextDialogFragment extends DialogFragment {

	private EditText mContentEdit;
	private String mContent;
	private static Callbacks mCallbacks;

    public interface Callbacks {
    	void onQueryUpdated(String query);
    }

    static QueryEditTextDialogFragment create(String content, Callbacks callbacks) {
    	QueryEditTextDialogFragment fragment = new QueryEditTextDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(Constants.ARG_DOCUMENT_CONTENT, content);
    	fragment.setArguments(args);
    	QueryEditTextDialogFragment.mCallbacks = callbacks;
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_query_text_edit, null);

    	mContent = getArguments().getString(Constants.ARG_DOCUMENT_CONTENT);
    	mContentEdit = (EditText)view.findViewById(R.id.query_edit_content);
    	mContentEdit.setText(mContent);
    	
    	return UiUtils.buildAlertDialog(view, android.R.drawable.ic_menu_edit, "Edit Query", true, 0, new UiUtils.AlertDialogCallbacks() {			
			@Override
			public boolean onOK() {
				mCallbacks.onQueryUpdated(mContentEdit.getText().toString());
				return true;
			}

			@Override
			public boolean onNeutralButton() {
				return true;
			}
		});    
    }
}
