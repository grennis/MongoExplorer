package com.innodroid.mongobrowser.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

public class BaseDialogFragment extends DialogFragment {
    protected View onCreateDialog(int layout) {
        View view = getActivity().getLayoutInflater().inflate(layout, null);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        ButterKnife.unbind(this);
    }
}
