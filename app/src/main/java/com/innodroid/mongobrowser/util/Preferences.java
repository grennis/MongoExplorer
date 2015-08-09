package com.innodroid.mongobrowser.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.innodroid.mongobrowser.R;

public class Preferences {
    private SharedPreferences mPrefs;
    private Context mContext;
    private int mDefaultDocumentPageSize;

    private static final String KEY_SHOW_SYSTEM_COLLECTIONS = "show_system_collections";
    private static final String KEY_DOCUMENT_PAGE_SIZE = "document_page_size";
    private static final String KEY_UPDATE_NOTICE_APPVER = "update_notice_ver";

    public Preferences(Context context) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mDefaultDocumentPageSize = context.getResources().getInteger(R.integer.default_document_page_size);
    }

    public boolean getShowSystemCollections() {
        return mPrefs.getBoolean(KEY_SHOW_SYSTEM_COLLECTIONS, false);
    }

    public void setShowSystemCollections(boolean value) {
        mPrefs.edit().putBoolean(KEY_SHOW_SYSTEM_COLLECTIONS, value).apply();
    }

    public int getDocumentPageSize() {
        return mPrefs.getInt(KEY_DOCUMENT_PAGE_SIZE, mDefaultDocumentPageSize);
    }

    public void setDocumentPageSize(int value) {
        mPrefs.edit().putInt(KEY_DOCUMENT_PAGE_SIZE, value).apply();
    }

    public boolean showUpdateNotice() {
        String lastShownVersion = mPrefs.getString(KEY_UPDATE_NOTICE_APPVER, "");
        String actualVersion = UiUtils.getAppVersionString(mContext);

        if (actualVersion.equals(lastShownVersion)) {
            return false;
        }

        mPrefs.edit().putString(KEY_UPDATE_NOTICE_APPVER, actualVersion).apply();
        return true;
    }
}
