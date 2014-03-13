package com.innodroid.mongobrowser.util;

import com.innodroid.mongobrowser.Constants;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class LeftMarginAnimation extends Animation {
	private int mFrom;
	private int mTo;
	private View mView;
	
	public LeftMarginAnimation(View view, int from, int to) {
		mView = view;
		mFrom = from;
		mTo = to;
		setDuration(Constants.SLIDE_ANIM_DURATION);
	}
	
	@Override
	public boolean willChangeBounds() {
		return true;
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		
		float diff = (mTo - mFrom) * interpolatedTime;
		
		((MarginLayoutParams)mView.getLayoutParams()).leftMargin = (int)(mFrom + diff);
		mView.requestLayout();
	}
}
