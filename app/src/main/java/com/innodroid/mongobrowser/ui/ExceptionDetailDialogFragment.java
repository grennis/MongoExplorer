package com.innodroid.mongobrowser.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.util.UiUtils;

import butterknife.Bind;

public class ExceptionDetailDialogFragment extends BaseDialogFragment {
	@Bind(R.id.exception_detail_title) TextView mTitle;
	@Bind(R.id.exception_detail_text) TextView mText;

	private static final String ARG_TITLE = "title";
	private static final String ARG_MESSAGE = "msg";
	
	public ExceptionDetailDialogFragment() {
		super();
	}
	
    public static ExceptionDetailDialogFragment newInstance(String title, Exception ex) {
    	ExceptionDetailDialogFragment fragment = new ExceptionDetailDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(ARG_TITLE, title);
    	args.putString(ARG_MESSAGE, ex.toString());
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = super.onCreateDialog(R.layout.fragment_exception_detail);

		mTitle.setText(getArguments().getString(ARG_TITLE));
    	mText.setText(getArguments().getString(ARG_MESSAGE));

    	return UiUtils.buildAlertDialog(view, R.drawable.ic_warning_black, R.string.error_has_occurred, false, 0, UiUtils.EmptyAlertCallbacks);
    }
}


