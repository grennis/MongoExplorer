package com.innodroid.mongobrowser;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.innodroid.mongobrowser.util.UiUtils;

public class QueryEditNameDialogFragment extends DialogFragment {
	private EditText mContentEdit;
	private String mContent;

    public interface Callbacks {
    	void onQueryNamed(String query);
    }
    
    public QueryEditNameDialogFragment() {
    	super();
    }

    static QueryEditNameDialogFragment create(String content) {
    	QueryEditNameDialogFragment fragment = new QueryEditNameDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(Constants.ARG_DOCUMENT_CONTENT, content);
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_query_name_edit, null);

    	mContent = getArguments().getString(Constants.ARG_DOCUMENT_CONTENT);
    	mContentEdit = (EditText)view.findViewById(R.id.query_edit_name);
    	mContentEdit.setText(mContent);
    	
    	return UiUtils.buildAlertDialog(view, android.R.drawable.ic_menu_edit, "Query Name", true, 0, new UiUtils.AlertDialogCallbacks() {			
			@Override
			public boolean onOK() {
				((Callbacks)getTargetFragment()).onQueryNamed(mContentEdit.getText().toString());
				return true;
			}

			@Override
			public boolean onNeutralButton() {
				return true;
			}
		});    
    }
}
