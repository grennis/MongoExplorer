package com.innodroid.mongobrowser;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.innodroid.mongobrowser.util.JsonUtils;

public class DocumentDetailFragment extends Fragment {

	private String mFormattedText;
	private TextView mContentText;
	private Callbacks mCallbacks;

    public interface Callbacks {
    	void onEditDocument(int position, String content);
    }

    public DocumentDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);    	
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_document_detail, container, false);
    	
    	mContentText = (TextView) view.findViewById(R.id.document_detail_content);
    	updateContent(getArguments().getString(Constants.ARG_DOCUMENT_CONTENT));
    	
        return view;
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	
    	mCallbacks = (Callbacks)activity;
    }
    
    @Override
    public void onDetach() {
    	super.onDetach();
    	
    	mCallbacks = null;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.document_detail_menu, menu);
    }    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_document_detail_edit:
    			mCallbacks.onEditDocument(getArguments().getInt(Constants.ARG_POSITION), mFormattedText);
    		default:
    	    	return super.onOptionsItemSelected(item);
    	}
    }
    
    public void updateContent(String json) {
    	mFormattedText = JsonUtils.prettyPrint(json);    	
    	mContentText.setText(mFormattedText);
    }
}


