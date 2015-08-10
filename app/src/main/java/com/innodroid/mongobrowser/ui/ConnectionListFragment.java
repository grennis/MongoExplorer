package com.innodroid.mongobrowser.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.data.MongoBrowserProvider;
import com.innodroid.mongobrowser.adapters.MongoConnectionAdapter;

import butterknife.OnClick;
import butterknife.OnItemClick;

public class ConnectionListFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private MongoConnectionAdapter mAdapter;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private long mSelectAfterLoad;

    public ConnectionListFragment() {
    }

    @NonNull
    public static ConnectionListFragment newInstance(boolean activateOnClick) {
        Bundle args = new Bundle();
        ConnectionListFragment fragment = new ConnectionListFragment();
        args.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, activateOnClick);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTitleText() {
        return R.string.connections;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
            mActivatedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (mAdapter == null) {
            mAdapter = new MongoConnectionAdapter(getActivity(), null, true);
            onRefresh();
        } else {
            mAdapter.notifyDataSetChanged();
        }

        mList.setAdapter(mAdapter);

        mList.setChoiceMode(getArguments().getBoolean(Constants.ARG_ACTIVATE_ON_CLICK)
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);

        if (mActivatedPosition != ListView.INVALID_POSITION) {
            setActivatedPosition(mActivatedPosition);
        }

        return view;
    }

    @Override
    public void onRefresh() {
        getLoaderManager().initLoader(0, null, this);
    }

    @OnClick(R.id.fab_add)
    public void clickAdd() {
        Events.postAddConnection();
    }

    @OnItemClick(android.R.id.list)
    public void onItemClick(int position) {
        setActivatedPosition(position);

        Events.postConnectionSelected(mAdapter.getItemId(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            mList.setItemChecked(mActivatedPosition, false);
        } else {
            mList.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
	public Loader<Cursor> onCreateLoader(int id, Bundle params) {
	    return new CursorLoader(getActivity(), MongoBrowserProvider.CONNECTION_URI, null, null, null, MongoBrowserProvider.NAME_CONNECTION_NAME);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mSwipeRefresh.setRefreshing(false);
		mAdapter.swapCursor(cursor);
		
		if (mSelectAfterLoad > 0)
			selectItem(cursor, mSelectAfterLoad);
		else
			setActivatedPosition(mActivatedPosition);
		
		mSelectAfterLoad = 0;
	}

	public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
	}

    public void onEvent(Events.ConnectionDeleted e) {
        mAdapter.notifyDataSetChanged();
    }

	private void selectItem(Cursor cursor, long id) {
		int pos = 0;
		int original = cursor.getPosition();
		if (!cursor.moveToFirst())
			return;

		do {
			if (cursor.getLong(MongoBrowserProvider.INDEX_CONNECTION_ID) == id)
				break;
			pos++;
		} while (cursor.moveToNext());
		
		cursor.moveToPosition(original);

        setActivatedPosition(pos);
	}

	public void reloadAndSelect(long id) {
		mSelectAfterLoad = id;
		getLoaderManager().initLoader(0, null, this);		
	}
}
