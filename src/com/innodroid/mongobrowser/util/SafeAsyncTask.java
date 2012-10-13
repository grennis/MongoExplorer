package com.innodroid.mongobrowser.util;

import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;

import com.innodroid.mongobrowser.ExceptionDetailDialogFragment;

public abstract class SafeAsyncTask<T, U, V> extends AsyncTask<T, U, V>{
	private Exception mException;
	protected FragmentManager mFM;
	
	protected abstract V safeDoInBackground(T... params) throws Exception;
	protected abstract void safeOnPostExecute(V result);
	protected abstract String getErrorTitle();

	public SafeAsyncTask(FragmentManager fm) {
		mFM = fm;
	}
	
	@Override
	protected V doInBackground(T... params) {
		mException = null;

		try {
			return safeDoInBackground(params);
		} catch (Exception ex) {
			ex.printStackTrace();
			mException = ex;
			return null;
		}
	}

	@Override
	protected void onPostExecute(V result) {
		super.onPostExecute(result);

		if (mException == null)
			safeOnPostExecute(result);
		else
			ExceptionDetailDialogFragment.create(getErrorTitle(), mException).show(mFM, null);
	}
}
