package com.innodroid.mongobrowser;

import android.app.Application;

import com.mongodb.MongoAndroid;

public class MongoExplorerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MongoAndroid.init();
    }
}
