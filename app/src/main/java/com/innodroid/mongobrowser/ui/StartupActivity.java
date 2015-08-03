package com.innodroid.mongobrowser.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.innodroid.mongobrowser.R;

public class StartupActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.multi_pane)) {
            startActivity(new Intent(this, MultiPaneActivity.class));
        } else {
            startActivity(new Intent(this, SinglePaneActivity.class));
        }

        finish();
    }
}
