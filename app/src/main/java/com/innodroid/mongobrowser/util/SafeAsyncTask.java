package com.innodroid.mongobrowser.util;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import com.innodroid.mongobrowser.ui.ExceptionDetailDialogFragment;

public abstract class SafeAsyncTask<T, U, V> extends AsyncTask<T, U, V>{
	private Exception mException;
	private FragmentActivity mFragmentActivity;
	private ProgressDialog mDialog;
	
	protected abstract V safeDoInBackground(T... params) throws Exception;
	protected abstract void safeOnPostExecute(V result);
	protected abstract String getErrorTitle();
	
	public SafeAsyncTask(FragmentActivity activity) {		
		mFragmentActivity = activity;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		String caption = getProgressMessage();
		
		if (caption != null)
			mDialog = ProgressDialog.show(mFragmentActivity, null, caption, true, false);		
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

		if (mDialog != null)
			mDialog.dismiss();

		if (mException == null)
			safeOnPostExecute(result);
		else
			ExceptionDetailDialogFragment.newInstance(getErrorTitle(), mException).show(mFragmentActivity.getSupportFragmentManager(), null);
	}

	protected String getProgressMessage() {
		return null;
	}
}
