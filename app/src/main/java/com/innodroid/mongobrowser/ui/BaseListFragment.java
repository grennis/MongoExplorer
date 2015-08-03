package com.innodroid.mongobrowser.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public abstract class BaseListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @Bind(android.R.id.list) ListView mList;
    @Bind(R.id.list_swipe_refresh) SwipeRefreshLayout mSwipeRefresh;

    protected boolean mActivateOnClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivateOnClick = getArguments().getBoolean(Constants.ARG_ACTIVATE_ON_CLICK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.fragment_generic_list, inflater, container, savedInstanceState);

        mSwipeRefresh.setOnRefreshListener(this);

        return view;
    }

    @Override
    public abstract void onRefresh();
}
