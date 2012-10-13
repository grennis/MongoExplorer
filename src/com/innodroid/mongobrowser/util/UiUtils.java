package com.innodroid.mongobrowser.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

import com.innodroid.mongobrowser.R;

public class UiUtils {
	public interface AlertDialogCallbacks {
		boolean onOK();
	}
	
	public static Dialog buildAlertDialog(View view, int icon, int title, final AlertDialogCallbacks callbacks) {
        final AlertDialog dialog = new AlertDialog.Builder(view.getContext())
	        .setIcon(icon)
	        .setView(view)
	        .setTitle(title)
	        .setCancelable(true)
	        .setPositiveButton(android.R.string.ok,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            }
	        )
	        .setNegativeButton(android.R.string.cancel,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            }
	        )
	        .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface di) {
                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    	if (callbacks.onOK())
                    		dialog.dismiss();
                    }
                });
            }
        });     
        
        return dialog;
	}

	public static void confirm(Context context, int message, final AlertDialogCallbacks callbacks) {
        new AlertDialog.Builder(context)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setMessage(message)
	        .setTitle(R.string.title_confirm)
	        .setCancelable(true)
	        .setPositiveButton(android.R.string.ok,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	callbacks.onOK();
	                }
	            }
	        )
	        .setNegativeButton(android.R.string.cancel,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            }
	        )
	        .create().show();
	}
}
