package com.innodroid.mongobrowser.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.util.Preferences;
import com.innodroid.mongobrowser.util.UiUtils;

import butterknife.Bind;

public class SettingsDialogFragment extends BaseDialogFragment {
    @Bind(R.id.settings_show_system_collections) public CheckBox mShowSystemCollections;
    @Bind(R.id.settings_document_load_size) public EditText mDocumentLoadSize;

    public static SettingsDialogFragment newInstance() {
        SettingsDialogFragment fragment = new SettingsDialogFragment();
        //Bundle args = new Bundle();
        //args.putString(Constants.ARG_DOCUMENT_CONTENT, content);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = super.onCreateDialog(R.layout.fragment_settings);

        final Preferences prefs = new Preferences(view.getContext());
        mShowSystemCollections.setChecked(prefs.getShowSystemCollections());
        mDocumentLoadSize.setText(String.valueOf(prefs.getDocumentPageSize()));

        return UiUtils.buildAlertDialog(view, R.drawable.ic_mode_edit_black, "Settings", true, 0, new UiUtils.AlertDialogCallbacks() {
            @Override
            public boolean onOK() {
                prefs.setShowSystemCollections(mShowSystemCollections.isChecked());
                prefs.setDocumentPageSize(Integer.parseInt(mDocumentLoadSize.getText().toString()));
                Events.postSettingsChanged();
                return true;
            }

            @Override
            public boolean onNeutralButton() {
                return true;
            }
        });
    }
}
