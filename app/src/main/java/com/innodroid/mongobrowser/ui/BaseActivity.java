package com.innodroid.mongobrowser.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;
import com.innodroid.mongobrowser.data.MongoData;
import com.innodroid.mongobrowser.util.Preferences;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public abstract class BaseActivity extends AppCompatActivity {
	private static final String STATE_COLLECTION_INDEX = "collname";

	@Bind(R.id.drawer_version) public TextView mAppVersion;
	@Bind(R.id.drawer_navigation) NavigationView mNavigationView;
	@Bind(R.id.drawer_layout) DrawerLayout mDrawer;
	@Bind(R.id.frame_1) public FrameLayout mFrame1;

	protected int mSelectedCollectionIndex;
	protected static boolean mHavePromptedToAddConnection = false;
	private CustomDrawerToggle mDrawerToggle;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResource());

		ButterKnife.bind(this);

		mAppVersion.setText(UiUtils.getAppVersionString(this));

		mDrawerToggle = new CustomDrawerToggle();
		mDrawer.setDrawerListener(mDrawerToggle);

		mNavigationView.setNavigationItemSelectedListener(NavigationItemSelected);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState == null) {
        	loadConnectionListPane();
        } else {
        	mSelectedCollectionIndex = savedInstanceState.getInt(STATE_COLLECTION_INDEX);
        }

		if (new Preferences(this).showUpdateNotice()) {
			UiUtils.message(this, R.string.new_version, R.string.new_version_detail, DismissNewVersion);
		}
    	else {
			checkAddConnection();
		}
    }

	private DialogInterface.OnClickListener DismissNewVersion = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			checkAddConnection();
		}
	};

	private void checkAddConnection() {
		if (!mHavePromptedToAddConnection) {
			mHavePromptedToAddConnection = true;
			new AddConnectionIfNoneExistTask().execute();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		ButterKnife.unbind(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		EventBus.getDefault().register(this);
	}

	@Override
	protected void onStop() {
		super.onStop();

		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_COLLECTION_INDEX, mSelectedCollectionIndex);
	}

	private NavigationView.OnNavigationItemSelectedListener NavigationItemSelected = new NavigationView.OnNavigationItemSelectedListener() {
		@Override
		public boolean onNavigationItemSelected(MenuItem menuItem) {
			closeDrawer();
			menuItem.setChecked(true);

			switch (menuItem.getItemId()) {
				case R.id.menu_drawer_home:
					home();
					return true;
				case R.id.menu_drawer_rate:
					rateApp();
					return true;
				case R.id.menu_drawer_settings:
					DialogFragment fragment = SettingsDialogFragment.newInstance();
					fragment.show(getSupportFragmentManager(), null);
					return true;
				default:
					return false;
			}
		}
	};

	protected void closeDrawer() {
		if (mDrawer.isDrawerOpen(mNavigationView)) {
			mDrawer.closeDrawer(mNavigationView);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	protected abstract int getLayoutResource();
	protected abstract void loadConnectionListPane();
	protected abstract void loadConnectionDetailsPane(long id);
	protected abstract void loadCollectionListPane(long connectionId);
	protected abstract void loadDocumentListPane(long connectionId, int collectionIndex);
	protected abstract void loadDocumentDetailsPane(int documentIndex);
	protected abstract void hideDocumentDetailPane();

	protected void home() {
		// Handle in derived class
	}

	public void onEvent(Events.ConnectionSelected e) {
		loadConnectionDetailsPane(e.ConnectionId);
	}

	private void showAddConnection() {
		DialogFragment fragment = ConnectionEditDialogFragment.newInstance(0);
		fragment.show(getSupportFragmentManager(), null);
	}

    public void onEvent(Events.AddConnection e) {
		showAddConnection();
    }

	public void onEvent(Events.AddDocument e) {
		DocumentEditDialogFragment fragment = DocumentEditDialogFragment.newInstance(mSelectedCollectionIndex, -1);
		fragment.show(getSupportFragmentManager(), null);
	}

	public void onEvent(Events.CollectionSelected e) {
		if (e.Index >= 0) {
			mSelectedCollectionIndex = e.Index;
			loadDocumentListPane(e.ConnectionId, e.Index);
		}
	}

	public void onEvent(Events.DocumentClicked e) {
		loadDocumentDetailsPane(e.Index);
	}

	public void onEvent(Events.Connected e) {
		loadCollectionListPane(e.ConnectionId);
	}

	public void onEvent(Events.ConnectionAdded e) {
		reloadConnectionListAndSelect(e.ConnectionId);
	}

	public void onEvent(Events.ConnectionUpdated e) {
		reloadConnectionListAndSelect(e.ConnectionId);
	}

	protected void reloadConnectionListAndSelect(long id) {
		if (isFinishing())
			return;
		
		ConnectionListFragment fragment = (ConnectionListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);
		
		if (fragment != null)
			fragment.reloadAndSelect(id);
	}

	public void onEvent(Events.DocumentDeleted e) {
		hideDocumentDetailPane();
		getSupportFragmentManager().executePendingTransactions();
	}

	public void onEvent(Events.EditDocument e) {
		DocumentEditDialogFragment fragment = DocumentEditDialogFragment.newInstance(mSelectedCollectionIndex, e.Index);
		fragment.show(getSupportFragmentManager(), null);
	}

	private void rateApp() {
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.innodroid.mongobrowser")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class AddConnectionIfNoneExistTask extends SafeAsyncTask<Void, Void, Boolean> {
		public AddConnectionIfNoneExistTask() {
			super(BaseActivity.this);
		}

		@Override
		protected Boolean safeDoInBackground(Void... arg0) {
			return new MongoBrowserProviderHelper(getContentResolver()).getConnectionCount() == 0;
		}

		@Override
		protected void safeOnPostExecute(Boolean res) {
			if (res) {
				showAddConnection();
			}
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Check Connections";
		}
    }

	private class CustomDrawerToggle extends ActionBarDrawerToggle {
		public CustomDrawerToggle() {
			super(BaseActivity.this, mDrawer, R.string.drawer_open, R.string.drawer_close);
		}

		public void onDrawerClosed(View view) {
			super.onDrawerClosed(view);
		}

		public void onDrawerOpened(View drawerView) {
			super.onDrawerOpened(drawerView);
			//Util.hideKeyboard(DrawerActivity.this);
		}
	}
}

