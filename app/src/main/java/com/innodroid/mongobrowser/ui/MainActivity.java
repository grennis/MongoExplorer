package com.innodroid.mongobrowser.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;

import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;
import com.innodroid.mongobrowser.util.LeftMarginAnimation;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.WidthAnimation;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {
	private static final String STATE_COLLECTION_NAME = "collname";
	
	private boolean mTwoPane;
	private int mScreenWidth;
	private int mLeftPaneWidth;
	private int mRightPaneWidth;
	private String mCollectionName;
    private FrameLayout mFrame1;
    private FrameLayout mFrame2;
    private FrameLayout mFrame3;
    private FrameLayout mFrame4;
    private static boolean mHavePromptedToAddConnection = false;

	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_generic);

        mFrame1 = (FrameLayout)findViewById(R.id.frame_1);
        mFrame2 = (FrameLayout)findViewById(R.id.frame_2);
        mFrame3 = (FrameLayout)findViewById(R.id.frame_3);
        mFrame4 = (FrameLayout)findViewById(R.id.frame_4);

        if (mFrame2 != null)
            mTwoPane = true;

        if (mTwoPane) {
        	setTitle(R.string.app_name);
        	mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
        	mLeftPaneWidth = mScreenWidth/2 - (mScreenWidth/10);
        	mRightPaneWidth = mScreenWidth - mLeftPaneWidth;
        }
        else {
        	setTitle(R.string.title_connection_list);
        }

        if (savedInstanceState == null) {
        	if (mTwoPane) {
	        	positionFramesOnScreen(mFrame1, mFrame2);
	        	moveOffscreenToRight(mFrame3);
	        	moveOffscreenToRight(mFrame4);
        	}

        	loadConnectionListPane();
        } else {
        	mCollectionName = savedInstanceState.getString(STATE_COLLECTION_NAME);

        	if (mTwoPane) {
	        	int depth = getSupportFragmentManager().getBackStackEntryCount();
	        	if (depth == 2) {
	            	moveOffscreenToLeft(mFrame1);
	            	moveOffscreenToLeft(mFrame2);
	            	positionFramesOnScreen(mFrame3, mFrame4);
	        	} else if (depth == 1) {
	            	moveOffscreenToLeft(mFrame1);
	            	positionFramesOnScreen(mFrame2, mFrame3);
	            	moveOffscreenToRight(mFrame4);
	        	} else {
	            	positionFramesOnScreen(mFrame1, mFrame2);
	            	moveOffscreenToRight(mFrame3);
	            	moveOffscreenToRight(mFrame4);
	        	}
        	}
        }

    	if (mHavePromptedToAddConnection)
    		setProgressBarIndeterminateVisibility(false);
    	else {
    		mHavePromptedToAddConnection = true;
    		new AddConnectionIfNoneExistTask().execute();
    	}
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

	private void moveOffscreenToLeft(View view) {
		((MarginLayoutParams)view.getLayoutParams()).leftMargin = mScreenWidth + 1;
	}

	private void moveOffscreenToRight(View view) {		
		((MarginLayoutParams)view.getLayoutParams()).leftMargin = mScreenWidth + 1;
	}

	private void positionFramesOnScreen(View left, View right) {
		left.getLayoutParams().width = mLeftPaneWidth;
    	((MarginLayoutParams)right.getLayoutParams()).leftMargin = mLeftPaneWidth;
    	right.getLayoutParams().width = mRightPaneWidth;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_COLLECTION_NAME, mCollectionName);
	}

	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();

		if (!mTwoPane) {
			if (fm.getBackStackEntryCount() > 0) {
				fm.popBackStack();
				return;
			}
		} else {
			if (fm.getBackStackEntryCount() > 1) {
				hideDocumentDetailPane();
				return;
			}

			if (fm.getBackStackEntryCount() > 0) {
				hideDocumentListPane();
				return;
			}
		}

		super.onBackPressed();
	}

	public void onEvent(Events.ConnectionSelected e) {
		loadConnectionDetailsPane(e.ConnectionId);
	}

	private void loadConnectionListPane() {
    	Bundle args = new Bundle();
        ConnectionListFragment fragment = new ConnectionListFragment();
        args.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, mTwoPane);
        fragment.setArguments(args);

		getSupportFragmentManager().beginTransaction().replace(R.id.frame_1, fragment).commit();
	}
	
    private void loadConnectionDetailsPane(long id) {
        Bundle arguments = new Bundle();
        arguments.putLong(Constants.ARG_CONNECTION_ID, id);
        ConnectionDetailFragment fragment = new ConnectionDetailFragment();
        fragment.setArguments(arguments);

		if (mTwoPane) {
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_2, fragment).commit();
		} else {
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_1, fragment).addToBackStack(null).commit();
		}
	}

    private void loadCollectionListPane(long connectionId) {
        Bundle arguments = new Bundle();
        CollectionListFragment fragment = new CollectionListFragment();
        arguments.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, true);
        arguments.putLong(Constants.ARG_CONNECTION_ID, connectionId);
        fragment.setArguments(arguments);

		if (mTwoPane) {
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_2, fragment).commit();
		} else {
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_1, fragment).addToBackStack(null).commit();
		}
    }

	private void showAddConnection() {
		DialogFragment fragment = ConnectionEditDialogFragment.create(0);
		fragment.show(getSupportFragmentManager(), null);
	}

    private void loadDocumentListPane(long connectionId, String collection) {
    	Bundle arguments = new Bundle();
        DocumentListFragment fragment = new DocumentListFragment();
        arguments.putString(Constants.ARG_COLLECTION_NAME, collection);
        arguments.putLong(Constants.ARG_CONNECTION_ID, connectionId);
        arguments.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, true);
        fragment.setArguments(arguments);

		FragmentManager fm = getSupportFragmentManager();

		if (mTwoPane) {
			boolean alreadyShiftedFrames = fm.getBackStackEntryCount() > 0;

			if (!alreadyShiftedFrames)
				shiftAllLeft(mFrame1, mFrame2, mFrame3);

			Fragment connectionList = fm.findFragmentById(R.id.frame_1);
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.frame_3, fragment);

			if (!alreadyShiftedFrames) {
				ft.remove(connectionList);
				ft.addToBackStack("doclist");
			}

			ft.commit();
		} else {
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_1, fragment).addToBackStack(null).commit();
		}
    }
    
	private void loadDocumentDetailsPane(String content) {
    	Bundle arguments = new Bundle();
        DocumentDetailFragment fragment = new DocumentDetailFragment();
        arguments.putString(Constants.ARG_COLLECTION_NAME, mCollectionName);
        arguments.putString(Constants.ARG_DOCUMENT_CONTENT, content);
        arguments.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, true);
        fragment.setArguments(arguments);

		FragmentManager fm = getSupportFragmentManager();

		if (mTwoPane) {
			boolean alreadyShiftedFrames = fm.getBackStackEntryCount() > 1;

			if (!alreadyShiftedFrames)
				shiftAllLeft(mFrame2, mFrame3, mFrame4);

			Fragment collectionList = fm.findFragmentById(R.id.frame_2);
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.frame_4, fragment);

			if (!alreadyShiftedFrames) {
				ft.remove(collectionList);
				ft.addToBackStack("docdetails");
			}

			ft.commit();
		} else {
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_1, fragment).addToBackStack(null).commit();
		}
    }

    private void hideDocumentListPane() {
    	getSupportFragmentManager().popBackStack();
    	shiftAllRight(mFrame1, mFrame2, mFrame3);
    }

    private void hideDocumentDetailPane() {
    	getSupportFragmentManager().popBackStack();
    	
    	// Pop the back stack isnt really enough since the fragment added in the transaction may have been replaced
    	FragmentManager fm = getSupportFragmentManager();
    	fm.beginTransaction().remove(fm.findFragmentById(R.id.frame_4)).commit();
    	shiftAllRight(mFrame2, mFrame3, mFrame4);    	
    }

    private void shiftAllLeft(View view1, View view2, View view3) {
		animateFromLeftPaneOffscreen(view1);
		animateFromRightPaneToLeftPane(view2);
		animateFromOffscreenToRightPane(view3);
    }
    
    private void shiftAllRight(View view1, View view2, View view3) {
		animateFromRightPaneOffscreen(view3);
		animateFromLeftPaneToRightPane(view2);
		animateFromOffscreenToLeftPane(view1);
    }
    
    private void animateFromOffscreenToRightPane(View view) {
    	((MarginLayoutParams)view.getLayoutParams()).width = mRightPaneWidth;
    	view.requestLayout();
    	
		Animation animation = new LeftMarginAnimation(view, mScreenWidth, mLeftPaneWidth);
		view.startAnimation(animation);
	}

	private void animateFromRightPaneToLeftPane(View view) {
		Animation translate = new LeftMarginAnimation(view, mLeftPaneWidth, 0);
		Animation width = new WidthAnimation(view, mRightPaneWidth, mLeftPaneWidth);
		
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(translate);
		set.addAnimation(width);
		view.startAnimation(set);
	}

	private void animateFromLeftPaneOffscreen(View view) {
		Animation animation = new LeftMarginAnimation(view, 0, -mLeftPaneWidth);
		view.startAnimation(animation);
	}

    private void animateFromOffscreenToLeftPane(View view) {
    	((MarginLayoutParams)view.getLayoutParams()).width = mLeftPaneWidth;
    	view.requestLayout();
    	
		Animation animation = new LeftMarginAnimation(view, -mLeftPaneWidth, 0);
		view.startAnimation(animation);
	}

	private void animateFromLeftPaneToRightPane(View view) {
		Animation translate = new LeftMarginAnimation(view, 0, mLeftPaneWidth);
		Animation width = new WidthAnimation(view, mLeftPaneWidth, mRightPaneWidth);
		
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(translate);
		set.addAnimation(width);
		view.startAnimation(set);
	}

	private void animateFromRightPaneOffscreen(View view) {
		Animation animation = new LeftMarginAnimation(view, mLeftPaneWidth, mScreenWidth+1);
		view.startAnimation(animation);
	}

    public void onEvent(Events.AddConnection e) {
		showAddConnection();
    }

	public void onEvent(Events.AddDocument e) {
		DocumentEditDialogFragment fragment = DocumentEditDialogFragment.create(mCollectionName, true, Constants.NEW_DOCUMENT_CONTENT_PADDED);
		fragment.show(getSupportFragmentManager(), null);
	}

	public void onEvent(Events.CollectionSelected e) {
		mCollectionName = e.CollectionName;
		loadDocumentListPane(e.ConnectionId, e.CollectionName);
	}

	public void onEvent(Events.DocumentClicked e) {
		loadDocumentDetailsPane(e.Content);
	}

	public void onEvent(Events.DocumentSelected e) {
		if (e.Content == null && !mTwoPane) {
			return;
		}

		// If nothing was selected (i.e., refresh) and we aren't showing the details pane, then dont shift to it
		if (e.Content == null && getSupportFragmentManager().getBackStackEntryCount() < 2)
			return;
		
		loadDocumentDetailsPane(e.Content);
	}

	public void onEvent(Events.Connected e) {
		loadCollectionListPane(e.ConnectionId);
	}

	public void onEvent(Events.ConnectionDeleted e) {
        getSupportFragmentManager().beginTransaction()
	        .remove(getSupportFragmentManager().findFragmentById(R.id.frame_2))
	        .commit();        
	}

	public void onEvent(Events.ConnectionAdded e) {
		reloadConnectionListAndSelect(e.ConnectionId);
	}

	public void onEvent(Events.ConnectionUpdated e) {
		reloadConnectionListAndSelect(e.ConnectionId);
	}

	private void reloadConnectionListAndSelect(long id) {
		if (isFinishing())
			return;
		
		ConnectionListFragment fragment = (ConnectionListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);
		
		if (fragment != null)
			fragment.reloadAndSelect(id);
        
        if (mTwoPane)
        	loadConnectionDetailsPane(id);
	}

	public void onEvent(Events.CollectionDropped e) {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 1) {
        	hideDocumentDetailPane();
        }
        
        fm.beginTransaction().remove(fm.findFragmentById(R.id.frame_3)).commit();
	}
	
	public void onEvent(Events.EditDocument e) {
		DocumentEditDialogFragment fragment = DocumentEditDialogFragment.create(mCollectionName, false, e.Content);
		fragment.show(getSupportFragmentManager(), null);
	}

	public void onEvent(Events.DocumentCreated e) {
		if (mTwoPane) {
			CollectionListFragment collectionList = (CollectionListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_2);
			if (collectionList != null)
				collectionList.reloadList();
		}
	}

	public void onEvent(Events.RefreshDocumentList e) {
		if (mTwoPane) {
			DocumentListFragment fragment = (DocumentListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_3);

			fragment.reloadList(getSupportFragmentManager().getBackStackEntryCount() > 1);

			CollectionListFragment collectionList = (CollectionListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_2);
			if (collectionList != null)
				collectionList.reloadList();
		} else {
			DocumentListFragment fragment = (DocumentListFragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);
			fragment.reloadList(false);

		}
	}
	
    private class AddConnectionIfNoneExistTask extends SafeAsyncTask<Void, Void, Boolean> {
		public AddConnectionIfNoneExistTask() {
			super(MainActivity.this);
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
}

