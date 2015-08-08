package com.innodroid.mongobrowser.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.innodroid.mongobrowser.R;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class BaseFragment extends Fragment {
    public int getTitleText() {
        return R.string.app_name;
    }

    protected View onCreateView(int layout, LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(layout, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.unbind(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    public void onEvent(Object e) {
        // Prevents event bus from firing exception that fragment has not event handlers
    }
}
