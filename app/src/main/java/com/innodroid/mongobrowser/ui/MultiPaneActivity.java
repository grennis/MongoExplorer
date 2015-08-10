package com.innodroid.mongobrowser.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.util.LeftMarginAnimation;
import com.innodroid.mongobrowser.util.WidthAnimation;

public class MultiPaneActivity extends BaseActivity {
	private int mScreenWidth;
	private int mLeftPaneWidth;
	private int mRightPaneWidth;
    private FrameLayout mFrame2;
    private FrameLayout mFrame3;
    private FrameLayout mFrame4;

	@Override
	protected int getLayoutResource() {
		return R.layout.activity_multi_pane;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
        super.onCreate(savedInstanceState);

        mFrame2 = (FrameLayout)findViewById(R.id.frame_2);
        mFrame3 = (FrameLayout)findViewById(R.id.frame_3);
        mFrame4 = (FrameLayout)findViewById(R.id.frame_4);

		mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		mLeftPaneWidth = mScreenWidth/2 - (mScreenWidth/10);
		mRightPaneWidth = mScreenWidth - mLeftPaneWidth;

        if (savedInstanceState == null) {
			positionFramesOnScreen(mFrame1, mFrame2);
			moveOffscreenToRight(mFrame3);
			moveOffscreenToRight(mFrame4);
        } else {
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
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();

		if (fm.getBackStackEntryCount() > 1) {
			hideDocumentDetailPane();
			return;
		}

		if (fm.getBackStackEntryCount() > 0) {
			hideDocumentListPane();
			return;
		}

		super.onBackPressed();
	}

	@Override
	protected void loadConnectionListPane() {
		ConnectionListFragment fragment = ConnectionListFragment.newInstance(true);

		getSupportFragmentManager().beginTransaction().replace(R.id.frame_1, fragment).commit();
	}

	@Override
	protected void loadConnectionDetailsPane(long id) {
		ConnectionDetailFragment fragment = ConnectionDetailFragment.newInstance(id);

		getSupportFragmentManager().beginTransaction().replace(R.id.frame_2, fragment).commit();
	}

	@Override
	protected void loadCollectionListPane(long connectionId) {
		CollectionListFragment fragment = CollectionListFragment.newInstance(connectionId, true);

		getSupportFragmentManager().beginTransaction().replace(R.id.frame_2, fragment).commit();
	}

	@Override
	protected void loadDocumentListPane(long connectionId, int collectionIndex) {
		DocumentListFragment fragment = DocumentListFragment.newInstance(connectionId, collectionIndex, true);

		FragmentManager fm = getSupportFragmentManager();

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
    }

	@Override
	protected void loadDocumentDetailsPane(int documentIndex) {
		DocumentDetailFragment fragment = DocumentDetailFragment.newInstance(mSelectedCollectionIndex, documentIndex);

		FragmentManager fm = getSupportFragmentManager();

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
	}

	private void hideDocumentListPane() {
    	getSupportFragmentManager().popBackStack();
		shiftAllRight(mFrame1, mFrame2, mFrame3);
    }

	@Override
	protected void hideDocumentDetailPane() {
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

	public void onEvent(Events.DocumentSelected e) {
		// If nothing was selected (i.e., refresh) and we aren't showing the details pane, then dont shift to it
		if (e.Index < 0 && getSupportFragmentManager().getBackStackEntryCount() < 2)
			return;
		
		loadDocumentDetailsPane(e.Index);
	}

	public void onEvent(Events.ConnectionDeleted e) {
		getSupportFragmentManager().beginTransaction()
			.remove(getSupportFragmentManager().findFragmentById(R.id.frame_2))
			.commit();
	}

	@Override
	protected void reloadConnectionListAndSelect(long id) {
		super.reloadConnectionListAndSelect(id);
		loadConnectionDetailsPane(id);
	}

	public void onEvent(Events.CollectionDropped e) {
        FragmentManager fm = getSupportFragmentManager();

		if (fm.getBackStackEntryCount() > 1) {
			hideDocumentDetailPane();
		}

		fm.beginTransaction().remove(fm.findFragmentById(R.id.frame_3)).commit();
	}
}

