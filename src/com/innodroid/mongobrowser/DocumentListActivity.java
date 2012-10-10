package com.innodroid.mongobrowser;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;

public class DocumentListActivity extends FragmentActivity implements CollectionListFragment.Callbacks {
    private long mSelectedDocumentID;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_document_list);
        setContentView(R.layout.activity_document_list);
        
        //LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshReceiver, new IntentFilter(Constants.MessageConnectionItemChanged));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//    	if (mTwoPane) {
//    		boolean enable = mSelectedID != 0;
//    		menu.getItem(1).setEnabled(enable);
//    		menu.getItem(2).setEnabled(enable);
//    	}
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//MenuInflater mi = getMenuInflater();
        //mi.inflate(R.menu.collection_list_menu, menu);
        
//    	if (!mTwoPane) {
//    		menu.getItem(1).setVisible(false);
//    		menu.getItem(2).setVisible(false);
//    	}
    	
        return true;
    }
        
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.connection_list_menu_add:
//            	Utils.addConnection(this);
//                return true;
//    		case R.id.connection_detail_menu_edit:
//    			Utils.editConnection(this, mSelectedID);
//    			return true;
//    		case R.id.connection_detail_menu_delete:
//    			Utils.deleteConnection(this, mSelectedID, false);
//    			return true;
//            case R.id.connection_list_menu_configure:
//            	Toast.makeText(this, "Configure", Toast.LENGTH_LONG).show();
//                return true;
//        }
//
//    	return super.onOptionsItemSelected(item);
//    }

	@Override
    public void onCollectionItemSelected(long id) {
//        if (mTwoPane) {
//        	loadDetailsPane(id);
//        	invalidateOptionsMenu();
//        } else {
//            Intent detailIntent = new Intent(this, ConnectionDetailActivity.class);
//            detailIntent.putExtra(ConnectionDetailFragment.ARG_CONNECTION_ID, id);
//            startActivity(detailIntent);
//        }
    }
//	
//    private void loadDetailsPane(long id) {
//    	mSelectedID = id;
//        Bundle arguments = new Bundle();
//        arguments.putLong(ConnectionDetailFragment.ARG_CONNECTION_ID, id);
//        ConnectionDetailFragment fragment = new ConnectionDetailFragment();
//        fragment.setArguments(arguments);
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.connection_detail_container, fragment)
//                .commit();
//	}
}
