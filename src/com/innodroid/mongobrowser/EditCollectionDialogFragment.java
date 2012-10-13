package com.innodroid.mongobrowser;

import com.innodroid.mongobrowser.util.UiUtils;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class EditCollectionDialogFragment extends DialogFragment {
	private TextView mNameView;
	private String mName;
	private static Callbacks mCallbacks;

	public interface Callbacks {
		void onCollectionEdited(int pos, String name);
	}
	
    static EditCollectionDialogFragment create(String name, Callbacks callbacks) {
    	EditCollectionDialogFragment fragment = new EditCollectionDialogFragment();
    	Bundle args = new Bundle();
    	args.putString(Constants.ARG_COLLECTION_NAME, name);
    	fragment.setArguments(args);
    	EditCollectionDialogFragment.mCallbacks = callbacks;
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_collection, null);

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

    	mCallbacks.onCollectionEdited(0, name);

    	return true;
    }
}


