package com.innodroid.mongobrowser.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.util.UiUtils;

import butterknife.Bind;

public class QueryEditNameDialogFragment extends BaseDialogFragment {
	@Bind(R.id.edit_query_name) EditText mContentEdit;

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
    	View view = super.onCreateDialog(R.layout.fragment_query_name_edit);

    	mContent = getArguments().getString(Constants.ARG_DOCUMENT_CONTENT);
    	mContentEdit.setText(mContent);
    	
    	return UiUtils.buildAlertDialog(view, R.drawable.ic_mode_edit_black, "Query Name", true, 0, new UiUtils.AlertDialogCallbacks() {
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
