package com.innodroid.mongobrowser;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;

import com.innodroid.mongobrowser.data.MongoBrowserProvider;

public class Utils {
    public static void addConnection(FragmentActivity activity) {
    	editConnection(activity, 0);
    }
    
    public static void editConnection(FragmentActivity activity, long id) {
        DialogFragment fragment = ConnectionSetupDialogFragment.create(id);
        fragment.show(activity.getSupportFragmentManager(), null);
    }
    
    public static void deleteConnection(final FragmentActivity activity, final long id, final boolean finishOnComplete) {
        new AlertDialog.Builder(activity)
	        .setIcon(android.R.drawable.ic_menu_delete)
	        .setMessage(R.string.confirm_delete_connection)
	        .setTitle(R.string.confirm_delete_title)
	        .setCancelable(true)
	        .setPositiveButton(android.R.string.ok,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	new DeleteConnectionTask(activity, id, finishOnComplete).execute();
	                }
	            }
	        )
	        .setNegativeButton(android.R.string.cancel,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	//
	                }
	            }
	        )
	        .create().show();
    }
    
    private static class DeleteConnectionTask extends AsyncTask<Void, Void, Boolean> {
    	private long mID;
    	private FragmentActivity mActivity;
    	private boolean mFinishOnComplete;
    	
    	public DeleteConnectionTask(FragmentActivity activity, long id, boolean finishOnComplete) {
    		mActivity = activity;
    		mID = id;
    		mFinishOnComplete = finishOnComplete;
    	}
    	
		@Override
		protected Boolean doInBackground(Void... arg0) {
			Uri uri = ContentUris.withAppendedId(MongoBrowserProvider.CONNECTION_URI, mID);
			mActivity.getContentResolver().delete(uri, null, null);
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
	    	Intent intent = new Intent(Constants.MessageConnectionItemChanged);
	    	LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
	    	
	    	if (mFinishOnComplete)
	    		mActivity.finish();
		}
    }    
}
