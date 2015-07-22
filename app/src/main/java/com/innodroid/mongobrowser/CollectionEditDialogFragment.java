package com.innodroid.mongobrowser;

import com.innodroid.mongobrowser.util.UiUtils;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CollectionEditDialogFragment extends DialogFragment {
	private TextView mNameView;
	private String mName;

	public interface Callbacks {
		void onCreateCollection(String name);
		void onRenameCollection(String name);
	}
	
	public CollectionEditDialogFragment() {
		super();
	}
	
    static CollectionEditDialogFragment create(String name, boolean isNew) {
    	CollectionEditDialogFragment fragment = new CollectionEditDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(Constants.ARG_COLLECTION_NAME, name);
    	args.putBoolean(Constants.ARG_IS_NEW, isNew);
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_collection_edit, null);

    	mName = getArguments().getString(Constants.ARG_COLLECTION_NAME);
    	mNameView = (TextView)view.findViewById(R.id.edit_collection_name);
    	mNameView.setText(mName);
    	
    	return UiUtils.buildAlertDialog(view, R.drawable.ic_mode_edit_black, R.string.title_edit_collection, true, 0, new UiUtils.AlertDialogCallbacks() {
			@Override
			public boolean onOK() {
				return save();
			}

			@Override
			public boolean onNeutralButton() {
				return false;
			}
		});    	
    }

    private boolean save() {
    	String name = mNameView.getText().toString();

    	if (name.length() == 0) {
    		Toast.makeText(getActivity(), "Required values not provided", Toast.LENGTH_SHORT).show();
    		return false;
    	}

    	Callbacks callbacks = (Callbacks)getTargetFragment();
    	if (getArguments().getBoolean(Constants.ARG_IS_NEW))
    		callbacks.onCreateCollection(name);
    	else
    		callbacks.onRenameCollection(name);

    	return true;
    }
}


