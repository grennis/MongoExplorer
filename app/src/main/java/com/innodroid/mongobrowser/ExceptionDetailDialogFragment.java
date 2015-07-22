package com.innodroid.mongobrowser;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.innodroid.mongobrowser.util.UiUtils;

public class ExceptionDetailDialogFragment extends DialogFragment {	
	private static final String ARG_TITLE = "title";
	private static final String ARG_MESSAGE = "msg";
	
	public ExceptionDetailDialogFragment() {
		super();
	}
	
    public static ExceptionDetailDialogFragment create(String title, Exception ex) {
    	ExceptionDetailDialogFragment fragment = new ExceptionDetailDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(ARG_TITLE, title);
    	args.putString(ARG_MESSAGE, ex.toString());
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_exception_detail, null);

    	TextView title = (TextView)view.findViewById(R.id.exception_detail_title);
    	TextView text = (TextView)view.findViewById(R.id.exception_detail_text);
    	
    	title.setText(getArguments().getString(ARG_TITLE));
    	text.setText(getArguments().getString(ARG_MESSAGE));

    	return UiUtils.buildAlertDialog(view, R.drawable.ic_warning_black, R.string.error_has_occurred, false, 0, UiUtils.EmptyAlertCallbacks);
    }
}


