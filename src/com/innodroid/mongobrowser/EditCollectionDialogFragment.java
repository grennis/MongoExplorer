package com.innodroid.mongobrowser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.innodroid.mongo.MongoHelper;

public class EditCollectionDialogFragment extends DialogFragment {
	private static String ARG_COLLECTION_POS = "collpos";
	private static String ARG_COLLECTION_NAME = "collname";
	
	private TextView mNameView;
	private static Callbacks mCallbacks;

	public interface Callbacks {
		void onCollectionEdited(int pos, String name);
	}
	
    static EditCollectionDialogFragment create(int pos, String name, Callbacks callbacks) {
    	EditCollectionDialogFragment fragment = new EditCollectionDialogFragment();
    	Bundle args = new Bundle();
    	args.putLong(ARG_COLLECTION_POS, pos);
    	args.putString(ARG_COLLECTION_NAME, name);
    	fragment.setArguments(args);
    	EditCollectionDialogFragment.mCallbacks = callbacks;
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_collection, null);

    	mNameView = (TextView)view.findViewById(R.id.edit_collection_name);
    	mNameView.setText(getArguments().getString(ARG_COLLECTION_NAME));
    	
        return new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_menu_edit)
                .setView(view)
                .setTitle(R.string.title_edit_collection)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	if (save())
                        		dialog.dismiss();
                        }
                    }
                )
                .setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	//
                        }
                    }
                )
                .create();
    }

    private boolean save() {
    	int pos = getArguments().getInt(ARG_COLLECTION_POS);
    	String oldName = getArguments().getString(ARG_COLLECTION_NAME);
    	String name = mNameView.getText().toString();

    	if (name.length() == 0) {
    		Toast.makeText(getActivity(), "Required values not provided", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	MongoHelper.renameCollection(oldName, name);
    	mCallbacks.onCollectionEdited(pos, name);

    	return true;
    }
}


