package com.innodroid.mongobrowser.ui;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.R;

import butterknife.Bind;

public class ChangeDatabaseDialogFragment extends BaseDialogFragment {
	@Bind(R.id.change_database_list) ListView mDatabaseListView;

	private static String ARG_DATABASES = "databases";
	
	public ChangeDatabaseDialogFragment() {
		super();
	}
	
    public static ChangeDatabaseDialogFragment newInstance(ArrayList<String> databases) {
    	ChangeDatabaseDialogFragment fragment = new ChangeDatabaseDialogFragment();
    	Bundle args = new Bundle();
    	args.putStringArrayList(ARG_DATABASES, databases);
    	fragment.setArguments(args);
    	return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = super.onCreateDialog(R.layout.fragment_change_database);

    	AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
	        .setIcon(R.drawable.ic_rotate_right_black)
	        .setView(view)
	        .setTitle(R.string.title_change_database)
	        .setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
						}
					}
			);

    	final Dialog dialog = builder.create();

		ArrayList<String> databases = getArguments().getStringArrayList(ARG_DATABASES);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, databases);

		mDatabaseListView.setAdapter(adapter);
		mDatabaseListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Events.postChangeDatabase(adapter.getItem(position));
				dialog.dismiss();
			}
		});
    	
    	return dialog;
    }
}
