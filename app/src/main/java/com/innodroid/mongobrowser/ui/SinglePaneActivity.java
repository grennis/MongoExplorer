package com.innodroid.mongobrowser.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.R;

public class SinglePaneActivity extends BaseActivity {
	@Override
	protected int getLayoutResource() {
		return R.layout.activity_single_pane;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportFragmentManager().addOnBackStackChangedListener(BackStackListener);
	}

	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();

		if (fm.getBackStackEntryCount() > 0) {
			fm.popBackStack();
			return;
		}

		super.onBackPressed();
	}

	@Override
	protected void loadConnectionListPane() {
		ConnectionListFragment fragment = ConnectionListFragment.newInstance(false);

		getSupportFragmentManager().beginTransaction().replace(R.id.frame_1, fragment).commit();
	}

	@Override
	protected void loadConnectionDetailsPane(long id) {
		ConnectionDetailFragment fragment = ConnectionDetailFragment.newInstance(id);

		loadContentPane(fragment);
	}

	@Override
	protected void loadCollectionListPane(long connectionId) {
		CollectionListFragment fragment = CollectionListFragment.newInstance(connectionId, false);

		loadContentPane(fragment);
    }

	@Override
    protected void loadDocumentListPane(long connectionId, int collectionIndex) {
		DocumentListFragment fragment = DocumentListFragment.newInstance(connectionId, collectionIndex, false);

		loadContentPane(fragment);
    }

	@Override
	protected void loadDocumentDetailsPane(int documentIndex) {
		DocumentDetailFragment fragment = DocumentDetailFragment.newInstance(mSelectedCollectionIndex, documentIndex);

		loadContentPane(fragment);
    }

	@Override
    protected void hideDocumentDetailPane() {
    	getSupportFragmentManager().popBackStack();
    }

	@Override
	public void onEvent(Events.DocumentDeleted e) {
		super.onEvent(e);

		DocumentListFragment f = (DocumentListFragment)getSupportFragmentManager().findFragmentById(R.id.frame_1);
		f.onEvent(e);
	}

	@Override
	protected void home() {
		clearBackStack();
	}

	private void clearBackStack() {
		FragmentManager fm = getSupportFragmentManager();

		while (fm.getBackStackEntryCount() > 0) {
			fm.popBackStackImmediate();
		}
	}

	public void onEvent(Events.CollectionDropped e) {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStackImmediate();
		CollectionListFragment fragment = (CollectionListFragment)fm.findFragmentById(R.id.frame_1);
		fragment.onEvent(e);
	}

	public void onEvent(Events.ConnectionDeleted e) {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStackImmediate();
		ConnectionListFragment fragment = (ConnectionListFragment)fm.findFragmentById(R.id.frame_1);
		fragment.onEvent(e);
	}

	private void loadContentPane(BaseFragment fragment) {
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame_1, fragment)
				.addToBackStack(null)
				.commit();
	}

	private FragmentManager.OnBackStackChangedListener BackStackListener = new FragmentManager.OnBackStackChangedListener() {
		@Override
		public void onBackStackChanged() {
			BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);

			if (fragment != null) {
				setTitle(fragment.getTitleText());
			}
		}
	};
}

