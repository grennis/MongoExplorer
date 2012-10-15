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
	private static Callbacks mCallbacks;

	public interface Callbacks {
		void onCreateCollection(String name);
		void onRenameCollection(String name);
	}
	
    static CollectionEditDialogFragment create(String name, boolean isNew, Callbacks callbacks) {
    	CollectionEditDialogFragment fragment = new CollectionEditDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(Constants.ARG_COLLECTION_NAME, name);
    	args.putBoolean(Constants.ARG_IS_NEW, isNew);
    	fragment.setArguments(args);
    	CollectionEditDialogFragment.mCallbacks = callbacks;
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_collection_edit, null);

    	mName = getArguments().getString(Constants.ARG_COLLECTION_NAME);
    	mNameView = (TextView)view.findViewById(R.id.edit_collection_name);
    	mNameView.setText(mName);
    	
    	return UiUtils.buildAlertDialog(view, android.R.drawable.ic_menu_edit, R.string.title_edit_collection, new UiUtils.AlertDialogCallbacks() {			
			@Override
			public boolean onOK() {
				return save();
			}
		});    	
    }

    private boolean save() {
    	String name = mNameView.getText().toString();

    	if (name.length() == 0) {
    		Toast.makeText(getActivity(), "Required values not provided", Toast.LENGTH_SHORT).show();
    		return false;
    	}

    	if (getArguments().getBoolean(Constants.ARG_IS_NEW))
    		mCallbacks.onCreateCollection(name);
    	else
    		mCallbacks.onRenameCollection(name);

    	return true;
    }
}


