package com.innodroid.mongobrowser.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.util.UiUtils;

import butterknife.Bind;

public class QueryEditTextDialogFragment extends BaseDialogFragment {
	@Bind(R.id.query_edit_content) EditText mQueryContent;

    public static QueryEditTextDialogFragment newInstance(String content) {
    	QueryEditTextDialogFragment fragment = new QueryEditTextDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(Constants.ARG_CONTENT, content);
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = super.onCreateDialog(R.layout.fragment_query_text_edit);

    	String content = getArguments().getString(Constants.ARG_CONTENT);
    	mQueryContent.setText(content);
    	
    	return UiUtils.buildAlertDialog(view, R.drawable.ic_mode_edit_black, "Edit Query", true, 0, new UiUtils.AlertDialogCallbacks() {
			@Override
			public boolean onOK() {
				Events.postQueryUpdated(mQueryContent.getText().toString());
				return true;
			}

			@Override
			public boolean onNeutralButton() {
				return true;
			}
		});    
    }
}
