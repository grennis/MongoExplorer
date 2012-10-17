package com.innodroid.mongobrowser.util;

import com.innodroid.mongobrowser.Constants;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class WidthAnimation extends Animation {
	private int mFrom;
	private int mTo;
	private View mView;
	
	public WidthAnimation(View view, int from, int to) {
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
		
		mView.getLayoutParams().width = (int)(mFrom + diff);
		mView.requestLayout();
	}
}
