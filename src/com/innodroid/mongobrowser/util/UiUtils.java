package com.innodroid.mongobrowser.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;

import com.innodroid.mongobrowser.R;

public class UiUtils {
	public interface AlertDialogCallbacks {
		boolean onOK();
		boolean onNeutralButton();
	}
	
	public interface ConfirmCallbacks {
		boolean onConfirm();
	}

	public static AlertDialogCallbacks EmptyAlertCallbacks = new AlertDialogCallbacks() {
		@Override
		public boolean onOK() {
			return true;
		}		

		@Override
		public boolean onNeutralButton() {
			return true;
		}		
	};
	
	public static Dialog buildAlertDialog(View view, int icon, int title, boolean hasCancel, int middleButtonText, final AlertDialogCallbacks callbacks) {
		return buildAlertDialog(view, icon, view.getResources().getString(title), hasCancel, middleButtonText, callbacks);
	}
	
	public static Dialog buildAlertDialog(View view, int icon, String title, boolean hasCancel, int middleButtonText, final AlertDialogCallbacks callbacks) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
	        .setIcon(icon)
	        .setView(view)
	        .setTitle(title)
	        .setPositiveButton(android.R.string.ok,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            }
	        );
        
        if (hasCancel) {
	        builder.setCancelable(true).setNegativeButton(android.R.string.cancel,
		            new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
		                }
		            }
		        );   	
        }

        if (middleButtonText != 0) {
	        builder.setNeutralButton(middleButtonText,
		            new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
		                }
		            }
		        );   	
        }

        final AlertDialog dialog = builder.create();

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

                b = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    	if (callbacks.onNeutralButton())
                    		dialog.dismiss();
                    }
                });
            }
        });     
        
        return dialog;
	}
	
	public static Dialog buildAlertDialog(Context context, ListAdapter adapter, OnClickListener listener, int icon, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
	        .setIcon(icon)
	        .setAdapter(adapter, listener)
	        .setTitle(title);
        
        builder.setCancelable(true);

        final AlertDialog dialog = builder.create();

        return dialog;
	}

	public static void message(Context context, int title, int message) {
        new AlertDialog.Builder(context)
	        .setIcon(android.R.drawable.ic_dialog_info)
	        .setMessage(message)
	        .setTitle(title)
	        .setCancelable(true)
	        .setPositiveButton(android.R.string.ok,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            }
	        )
	        .create().show();
	}

	public static void confirm(Context context, int message, final ConfirmCallbacks callbacks) {
        new AlertDialog.Builder(context)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setMessage(message)
	        .setTitle(R.string.title_confirm)
	        .setCancelable(true)
	        .setPositiveButton(android.R.string.ok,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	callbacks.onConfirm();
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
