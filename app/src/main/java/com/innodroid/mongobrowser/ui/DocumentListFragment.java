package com.innodroid.mongobrowser.ui;


import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.innodroid.mongobrowser.Events;
import com.innodroid.mongobrowser.data.MongoCollection;
import com.innodroid.mongobrowser.data.MongoData;
import com.innodroid.mongobrowser.data.MongoDocument;
import com.innodroid.mongobrowser.util.MongoHelper;
import com.innodroid.mongobrowser.Constants;
import com.innodroid.mongobrowser.R;
import com.innodroid.mongobrowser.data.MongoBrowserProvider;
import com.innodroid.mongobrowser.data.MongoBrowserProviderHelper;
import com.innodroid.mongobrowser.adapters.MongoDocumentAdapter;
import com.innodroid.mongobrowser.adapters.MongoQueryAdapter;
import com.innodroid.mongobrowser.util.Preferences;
import com.innodroid.mongobrowser.util.SafeAsyncTask;
import com.innodroid.mongobrowser.util.UiUtils;
import com.innodroid.mongobrowser.util.UiUtils.ConfirmCallbacks;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import butterknife.OnItemClick;

public class DocumentListFragment extends BaseListFragment {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String STATE_QUERY_ID = "query_id";
    private static final String STATE_QUERY_NAME = "query_name";
    private static final String STATE_QUERY_TEXT = "query_text";

    private long mConnectionId;
    private int mCollectionIndex;
    private long mQueryID;
    private String mQueryName;
    private String mQueryText;
    private MongoDocumentAdapter mAdapter;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private int mStart = 0;
    private int mTake = 5;

    public DocumentListFragment() {
    }

	@NonNull
	public static DocumentListFragment newInstance(long connectionId, int collectionIndex, boolean mTwoPane) {
		Bundle arguments = new Bundle();
		DocumentListFragment fragment = new DocumentListFragment();
		arguments.putInt(Constants.ARG_COLLECTION_INDEX, collectionIndex);
		arguments.putLong(Constants.ARG_CONNECTION_ID, connectionId);
		arguments.putBoolean(Constants.ARG_ACTIVATE_ON_CLICK, mTwoPane);
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public int getTitleText() {
		return R.string.documents;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		
		mTake = new Preferences(getActivity()).getDocumentPageSize();
    	mCollectionIndex = getArguments().getInt(Constants.ARG_COLLECTION_INDEX);
    	mConnectionId = getArguments().getLong(Constants.ARG_CONNECTION_ID);

		if (savedInstanceState != null) {
			mActivatedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
			mQueryID = savedInstanceState.getLong(STATE_QUERY_ID);
			mQueryName = savedInstanceState.getString(STATE_QUERY_NAME);
			mQueryText = savedInstanceState.getString(STATE_QUERY_TEXT);
			getActivity().invalidateOptionsMenu();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		if (mAdapter == null) {
			mAdapter = new MongoDocumentAdapter(getActivity());
			mAdapter.setItems(MongoData.Documents);
			onRefresh();
		} else {
			mAdapter.notifyDataSetChanged();
		}

		mList.setAdapter(mAdapter);

		mList.setChoiceMode(mActivateOnClick
				? ListView.CHOICE_MODE_SINGLE
				: ListView.CHOICE_MODE_NONE);

		if (mActivatedPosition != ListView.INVALID_POSITION) {
			setActivatedPosition(mActivatedPosition);
		}

		return view;
	}

	@Override
	public void onRefresh() {
		mStart = 0;
		mAdapter.removeAll();
		new LoadNextDocumentsTask(false).execute();
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.document_list_menu, menu);
    }    

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	boolean haveQuery = mQueryID != 0;
		menu.findItem(R.id.menu_document_list_clear_query).setEnabled(haveQuery);
		menu.findItem(R.id.menu_document_list_save_query).setEnabled(haveQuery);
		menu.findItem(R.id.menu_document_list_edit_query).setEnabled(haveQuery);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {    	
        switch (item.getItemId()) {
    		case R.id.menu_document_list_new_query:
    			newQuery();
    			return true;
    		case R.id.menu_document_list_load_query:
    			loadQuery();
    			return true;
    		case R.id.menu_document_list_edit_query:
    			editQuery();
    			return true;
    		case R.id.menu_document_list_save_query:
    			saveQuery();
    			return true;
    		case R.id.menu_document_list_clear_query:
    			clearQuery();
    			return true;
    		case R.id.menu_document_list_edit:
    			editCollection();
    			return true;
    		case R.id.menu_document_list_delete:
    			dropCollection();
    			return true;
    		default:
    	    	return super.onOptionsItemSelected(item);
        }
    }

	private void newQuery() {
		mQueryID = 0;
		mQueryName = null ;
		mQueryText = null;
		editQuery();
	}
	
	private void loadQuery() {
		String collectionName = MongoData.Collections.get(mCollectionIndex).Name;
		Cursor cursor = new MongoBrowserProviderHelper(getActivity().getContentResolver()).getNamedQueries(mConnectionId, collectionName);
		
		if (cursor.getCount() == 0) {
			UiUtils.message(getActivity(), R.string.load_query, R.string.no_saved_queries);
			return;
		}
		
		final MongoQueryAdapter adapter = new MongoQueryAdapter(getActivity(), cursor, false);
		
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Cursor cursor = adapter.getCursor();
				cursor.moveToPosition(which);
				loadQuery(cursor);
			}		
		};
		
		UiUtils.buildAlertDialog(getActivity(), adapter, listener, R.drawable.ic_file_download_black, "Load Query").show();
	}
	
	private void loadQuery(Cursor cursor) {
		mQueryID = cursor.getLong(MongoBrowserProvider.INDEX_QUERY_ID);
		mQueryName = cursor.getString(MongoBrowserProvider.INDEX_QUERY_NAME);
		mQueryText = cursor.getString(MongoBrowserProvider.INDEX_QUERY_TEXT);
		reloadList(true);
		getActivity().invalidateOptionsMenu();
	}
	
    private void editQuery() {
		String query = (mQueryText == null) ? Constants.NEW_DOCUMENT_CONTENT_PADDED : mQueryText;
		QueryEditTextDialogFragment dialog = QueryEditTextDialogFragment.newInstance(query);
		dialog.show(getFragmentManager(), null);
	}

    private void saveQuery() {
    	if (mQueryName == null) {
    		new GetUniqueQueryName().execute();
    		return;
    	}
    	
    	QueryEditNameDialogFragment dialog = QueryEditNameDialogFragment.newInstance(mQueryName);
    	dialog.show(getFragmentManager(), null);
	}

	public void clearQuery() {
		mQueryID = 0;
		mQueryName = null;
		mQueryText = null;
		reloadList(true);
		getActivity().invalidateOptionsMenu();
	}

	@OnClick(R.id.fab_add)
	public void clickAdd() {
		Events.postAddDocument();
	}

	public void onEvent(Events.QueryNamed e) {
		mQueryName = e.Name;
		new SaveQuery().execute();
	}

	public void onEvent(Events.DocumentCreated e) {
		mAdapter.insert(0, new MongoDocument(e.Content));
		setActivatedPosition(0);

		if (mActivateOnClick) {
			Events.postDocumentSelected(0);
		}
	}

	public void onEvent(Events.DocumentEdited e) {
		mAdapter.notifyDataSetChanged();

		Events.postDocumentSelected(e.Index);
	}

	public void onEvent(Events.DocumentDeleted e) {
		mAdapter.delete(mActivatedPosition);

		if (!mActivateOnClick) {
			Events.postDocumentSelected(-1);
			mActivatedPosition = ListView.INVALID_POSITION;
		}
	}

	public void onEvent(Events.RenameCollection e) {
		new RenameCollectionTask().execute(e.Name);
	}

	public void onEvent(Events.QueryUpdated e) {
		mQueryText = e.Content;

		if (mQueryID == 0)
			mQueryID = -1;

		reloadList(true);
		getActivity().invalidateOptionsMenu();
	}

	private void editCollection() {
        DialogFragment fragment = CollectionEditDialogFragment.newInstance(mCollectionIndex);
        fragment.show(getFragmentManager(), null);
	}

    private void dropCollection() {
    	UiUtils.confirm(getActivity(), R.string.confirm_drop_collection, new ConfirmCallbacks() {
			@Override
			public boolean onConfirm() {
				if (mAdapter.getCount() == 0) {
					new DropCollectionTask().execute();
					return true;
				}

				reconfirmDropCollection();
				return true;
			}
		});
	}

    private void reconfirmDropCollection() {
    	UiUtils.confirm(getActivity(), R.string.really_confirm_drop_collection, new ConfirmCallbacks() {
			@Override
			public boolean onConfirm() {
				new DropCollectionTask().execute();
				return true;
			}
		});
	}

	@OnItemClick(android.R.id.list)
	public void onItemClick(int position) {
        if ((mAdapter.isShowingLoadMore()) && (position == mAdapter.getCount()-1)) {
        	mStart += mTake;
        	new LoadNextDocumentsTask(false).execute();
        } else {
        	setActivatedPosition(position);
        	
			Events.postDocumentClicked(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
            outState.putLong(STATE_QUERY_ID, mQueryID);
            outState.putString(STATE_QUERY_NAME, mQueryName);
            outState.putString(STATE_QUERY_TEXT, mQueryText);
        }
    }
    
    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            mList.setItemChecked(mActivatedPosition, false);
        } else {
			mList.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

	public void reloadList(boolean trySelectAfterLoad) {
		boolean selectAfterLoad = trySelectAfterLoad && (mActivatedPosition != ListView.INVALID_POSITION);
		setActivatedPosition(ListView.INVALID_POSITION);
		Events.postDocumentSelected(0);
		mAdapter.removeAll();
		mStart = 0;
		new LoadNextDocumentsTask(selectAfterLoad).execute();		
	}

    private class RenameCollectionTask extends SafeAsyncTask<String, Void, String> {
    	public RenameCollectionTask() {
			super(getActivity());
		}

		@Override
		protected String safeDoInBackground(String... args) throws UnknownHostException {
			String newName = args[0];
			MongoCollection collection = MongoData.Collections.get(mCollectionIndex);
			MongoHelper.renameCollection(collection.Name, newName);
			collection.Name = newName;
			return newName;
		}

		@Override
		protected void safeOnPostExecute(String result) {
			Events.postCollectionRenamed(result);
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Rename";
		}		
    }

    private class DropCollectionTask extends SafeAsyncTask<Void, Void, Void> {
    	public DropCollectionTask() {
			super(getActivity());
		}

		@Override
		protected Void safeDoInBackground(Void... args) {
			String collectionName = MongoData.Collections.get(mCollectionIndex).Name;
			MongoHelper.dropCollection(collectionName);
			MongoData.Collections.remove(mCollectionIndex);
			return null;
		}

		@Override
		protected void safeOnPostExecute(Void result) {
			Events.postCollectionDropped();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Drop";
		}		
    }

    private class LoadNextDocumentsTask extends SafeAsyncTask<Void, Void, List<MongoDocument>> {
    	private boolean mSelectAfterLoad;
    	
    	public LoadNextDocumentsTask(boolean selectAfterLoad) {
			super(getActivity());
			
			mSelectAfterLoad = selectAfterLoad;
		}

		@Override
		protected List<MongoDocument> safeDoInBackground(Void... args) {
			if (mCollectionIndex < 0)
				return new ArrayList<>();

			String collectionName = MongoData.Collections.get(mCollectionIndex).Name;
			return MongoHelper.getPageOfDocuments(collectionName, mQueryText, mStart, mTake);
		}

		@Override
		protected void safeOnPostExecute(List<MongoDocument> results) {
			mSwipeRefresh.setRefreshing(false);

			MongoData.Documents.addAll(results);
			mAdapter.notifyDataSetChanged();

			if (results.size() < mTake)
				mAdapter.showLoadMore(false);
			
			if (mSelectAfterLoad && results.size() > 0) {
				setActivatedPosition(0);
				Events.postDocumentSelected(0);
			}
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Load";
		}		
    }
    
    private class GetUniqueQueryName extends SafeAsyncTask<Void, Void, String> {
    	public GetUniqueQueryName() {
			super(getActivity());
    	}

		@Override
		protected String safeDoInBackground(Void... args) {
			String name = "Query ";
			String collectionName = MongoData.Collections.get(mCollectionIndex).Name;
			MongoBrowserProviderHelper helper = new MongoBrowserProviderHelper(getActivity().getContentResolver());
			
			int i = 1;
			while (true)
			{
				String tryName = name + i;
				Cursor cursor = helper.findQuery(tryName, mConnectionId, collectionName);
				boolean taken = cursor.moveToFirst();
				cursor.close();
				
				if (!taken)
					return tryName;
			
				i++;
			}
		}

		@Override
		protected void safeOnPostExecute(String results) {
			mQueryName = results;
			saveQuery();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Get Query Name";
		}		
    }
    
    private class SaveQuery extends SafeAsyncTask<Void, Void, Void> {
    	public SaveQuery() {
			super(getActivity());
    	}

		@Override
		protected Void safeDoInBackground(Void... args) {
			String collectionName = MongoData.Collections.get(mCollectionIndex).Name;
			MongoBrowserProviderHelper helper = new MongoBrowserProviderHelper(getActivity().getContentResolver());
			mQueryID = helper.saveQuery(mQueryID, mQueryName, mConnectionId, collectionName, mQueryText);
			return null;
		}

		@Override
		protected void safeOnPostExecute(Void args) {
			Toast.makeText(getActivity(), "Query Saved", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected String getErrorTitle() {
			return "Failed to Save";
		}		
    }
}
