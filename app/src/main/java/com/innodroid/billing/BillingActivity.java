package com.innodroid.billing;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.innodroid.billing.BillingService.RequestPurchase;
import com.innodroid.billing.BillingService.RestoreTransactions;
import com.innodroid.billing.Consts.PurchaseState;
import com.innodroid.billing.Consts.ResponseCode;
import com.innodroid.mongobrowser.Constants;

public abstract class BillingActivity extends FragmentActivity {

    private Handler mHandler;
    private BillingObserver mObserver;
    private BillingService mService;

	protected static final int DIALOG_CANT_CONNECT = 201;
	protected static final int DIALOG_NO_BILLING_SUPPORT = 202;
	protected static final int DIALOG_PURCHASE_COMPLETE = 203;
	
	private static final String PREF_TRANSACTIONS_RESTORED = "transRestored";
	private static final String PREF_PURCHASED = "purchased";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        mHandler = new Handler();
        mObserver = new BillingObserver(mHandler);

        mService = new BillingService();
        mService.setContext(this);
        
        restoreTransactionsIfNecessary();
	}
	
    @Override
    protected void onStart() {
        super.onStart();
        
        if (mObserver != null)
        	ResponseHandler.register(mObserver);        
    }
	
    @Override
    protected void onStop() {
        super.onStop();
        
        if (mObserver != null)
        	ResponseHandler.unregister(mObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (mService != null)
        	mService.unbind();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_CANT_CONNECT:
            return createDialog("Can\'t connect to Market", "This app cannot connect to Market. Your version of Market may be out of date. You can continue to use this app but you won\'t be able to make purchases.", android.R.drawable.stat_sys_warning, true);
        case DIALOG_NO_BILLING_SUPPORT:
            return createDialog("Can\'t make purchases", "The Market billing service is not available at this time.  You can continue to use this app but you won\'t be able to make purchases", android.R.drawable.ic_dialog_alert, true);
        case DIALOG_PURCHASE_COMPLETE:
            return createDialog("Purchase Complete", getPurchaseCompleteMessage(), android.R.drawable.ic_dialog_info, false);
        default:
            return null;
        }    	
    }
    
    private Dialog createDialog(String title, String message, int icon, boolean showHelp) {
        String helpUrl = replaceLanguageAndRegion("http://market.android.com/support/bin/answer.py?answer=1050566&amp;hl=%lang%&amp;dl=%region%");
        if (Consts.DEBUG) {
            Log.i(Constants.LOG_TAG, helpUrl);
        }
        final Uri helpUri = Uri.parse(helpUrl);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
            .setIcon(icon)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null);
        
        if (showHelp) {
            builder.setNegativeButton("Learn more", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, helpUri);
                    startActivity(intent);
                }
            });        	
        }
        
        return builder.create();
    }
    
    private String replaceLanguageAndRegion(String str) {
        // Substitute language and or region if present in string
        if (str.contains("%lang%") || str.contains("%region%")) {
            Locale locale = Locale.getDefault();
            str = str.replace("%lang%", locale.getLanguage().toLowerCase());
            str = str.replace("%region%", locale.getCountry().toLowerCase());
        }
        return str;
    }

    protected void restoreTransactionsIfNecessary() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BillingActivity.this);
        
        if (!prefs.getBoolean(PREF_TRANSACTIONS_RESTORED, false)) {
        	mService.restoreTransactions();	
        }
    }
    
    protected void beginPurchase() {
		if (!mService.checkBillingSupported()) {
            showDialog(DIALOG_CANT_CONNECT);
        }    	
    }
    
    protected abstract String getProductId();
    protected abstract String getPurchaseCompleteMessage();
    
    protected void onPurchaseRequested()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BillingActivity.this);
		prefs.edit().putBoolean(PREF_PURCHASED, true).commit();
		
		showDialog(DIALOG_PURCHASE_COMPLETE);
    }
    
    protected void onPurchaseCompleted()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BillingActivity.this);
		prefs.edit().putBoolean(PREF_PURCHASED, true).commit();    	
    }
    
    protected void onPurchaseFailed()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BillingActivity.this);
		prefs.edit().putBoolean(PREF_PURCHASED, false).commit();
    }

    protected void onRestoreTransactionsCompleted()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BillingActivity.this);
        prefs.edit().putBoolean(PREF_TRANSACTIONS_RESTORED, true).commit();
    }
    
    protected boolean hasPurchased() {
    	return PreferenceManager.getDefaultSharedPreferences(BillingActivity.this).getBoolean(PREF_PURCHASED, false);
    }
    
    private class BillingObserver extends PurchaseObserver {
        public BillingObserver(Handler handler) {
            super(BillingActivity.this, handler);
        }

        @Override
        public void onBillingSupported(boolean supported) {
            if (Consts.DEBUG) {
                Log.i(Constants.LOG_TAG, "supported: " + supported);
            }
            
            if (supported) {
            	mService.requestPurchase(getProductId(), null);
            } else {
                showDialog(DIALOG_NO_BILLING_SUPPORT);
            }
        }

        @Override
        public void onPurchaseStateChange(PurchaseState purchaseState, String itemId, int quantity, long purchaseTime, String developerPayload) {
            if (Consts.DEBUG) {
                Log.i(Constants.LOG_TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
            }

        	if (purchaseState == PurchaseState.PURCHASED) {
        		onPurchaseCompleted();
        	}
        	else {
        		onPurchaseFailed();
        	}        	
        }

        @Override
        public void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
            if (Consts.DEBUG) {
                Log.d(Constants.LOG_TAG, request.mProductId + ": " + responseCode);
            }
            if (responseCode == ResponseCode.RESULT_OK) {
                if (Consts.DEBUG) {
                    Log.i(Constants.LOG_TAG, "purchase was successfully sent to server");
                }

                onPurchaseRequested();

                //logProductActivity(request.mProductId, "sending purchase request");
            } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
                if (Consts.DEBUG) {
                    Log.i(Constants.LOG_TAG, "user canceled purchase");
                }
                //logProductActivity(request.mProductId, "dismissed purchase dialog");
            } else {
                if (Consts.DEBUG) {
                    Log.i(Constants.LOG_TAG, "purchase failed");
                }
                //logProductActivity(request.mProductId, "request purchase returned " + responseCode);
            }
        }

        @Override
        public void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode) {
            if (responseCode == ResponseCode.RESULT_OK) {
                if (Consts.DEBUG) {
                    Log.d(Constants.LOG_TAG, "completed RestoreTransactions request");
                }

                onRestoreTransactionsCompleted();
            } else {
                if (Consts.DEBUG) {
                    Log.d(Constants.LOG_TAG, "RestoreTransactions error: " + responseCode);
                }
            }
        }
    }
}
