package com.innodroid.mongobrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class MongoConnectionListActivity extends FragmentActivity
        implements MongoConnectionListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mongoconnection_list);

        if (findViewById(R.id.mongoconnection_detail_container) != null) {
            mTwoPane = true;
            ((MongoConnectionListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mongoconnection_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(MongoConnectionDetailFragment.ARG_ITEM_ID, id);
            MongoConnectionDetailFragment fragment = new MongoConnectionDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mongoconnection_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, MongoConnectionDetailActivity.class);
            detailIntent.putExtra(MongoConnectionDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
