package net.classicgarage.truerandommusicplayer;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class FolderPicker extends DialogPreference
implements OnItemClickListener {

	private ListView mFoldersListView;
	private TextView mCurrentFolder;
	private Folder mPath;
	private File mRootSDCard;
	private FolderAdapter mAdapter;

	private static final String TAG="FolderPicker";
	

	public FolderPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		PlayerApplication app = PlayerApplication.getInstance();
		
		if (app.isExternalStorageAvail()) {
	        String prefDir = app.getPreferences().getString(PlayerApplication.PREF_MUSICDIRECTORIES, 
		        		app.getRootSDCard().getAbsolutePath());
				mPath=new Folder(prefDir);
		}
		else {
			Toast.makeText(context, "SD card appears to be unavailable. Please fix the problem and retry.", Toast.LENGTH_SHORT).show();
			mPath=null;
		}
	}
	
	@Override
	protected View onCreateDialogView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());

		View view = layoutInflater.inflate(
				R.layout.folders, null);


		mCurrentFolder = (TextView) view.findViewById(R.id.current_folder);						
		mFoldersListView = (ListView) view.findViewById(R.id.folders);
		mFoldersListView.setOnItemClickListener(this);

		// we start on the root of SD card
		mAdapter = new FolderAdapter();
		mFoldersListView.setAdapter(mAdapter);

		updateAdapter();

		return view;
	}

	/**
	 * {@inheritDoc}
	 */
	public void onDialogClosed (boolean positiveResult) {
		Log.d(TAG, "onDialogClosed : result="+positiveResult);

		if (positiveResult)  {
			Log.d(TAG, "onDialogClosed : pref value persisted="+mPath.getAbsolutePath());

			if (shouldPersist())
				persistString(mPath.getAbsolutePath());

			Toast.makeText(this.getContext(), "File location is now set to "+mPath.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		}
		super.onDialogClosed(positiveResult);
	}

	/*
	 * fills adapter with mPath directory list
	 */
	private void updateAdapter() {
		Log.d(TAG, "updateAdapter");

		mCurrentFolder.setText("current folder: "+mPath.getAbsolutePath());
		mAdapter.clear();
		if (!mPath.equals(mRootSDCard)) {
			mAdapter.add(new Folder(mPath, true));
		}
		File[] dirs = mPath.listFiles(mDirFilter);
		Arrays.sort(dirs);
		for (int i = 0; i < dirs.length; i++) {
			mAdapter.add(new Folder(dirs[i]));
		}

		mAdapter.notifyDataSetChanged();
		mFoldersListView.setSelection(0);
		//mFoldersListView.startLayoutAnimation();
	}

	/*
	 * click : navigate to sub-directory
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "onItemLongClick");
		
		mPath = (Folder) mAdapter.getItem(position);
		updateAdapter();
	}

	/*
	 * directories filter
	 */
	private FileFilter mDirFilter = new FileFilter() {
		public boolean accept(File file) {
			if (!file.getName().startsWith(".") && file.isDirectory())
				return true;
			else
				return false;
		}
	};

	/*
	 * inner class adapter for folders
	 */
	class FolderAdapter extends BaseAdapter {
		ArrayList<Folder> folders = new ArrayList<Folder>();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		
		public int getCount() {
			return folders.size();
		}

		public void add(Folder folder) {
			folders.add(folder);
		}

		public void clear() {
			folders.clear();
		}

		public Object getItem(int position) {
			return folders.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		/*
		 * display item in list
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "getView : position="+position);
			
			View v = inflater.inflate(R.layout.folder, parent, false);
			Folder folder = folders.get(position);
			
			TextView name = (TextView) v.findViewById(R.id.folder_name);
			name.setText(folder.isParent?"[..]":folder.getName());
 
			return v;
		}
	}
	
	// @SuppressWarnings("serial")
	// To ensure a serialized class is consistent with how the Java runtime perceives this object
	// (based on its class definition), it is important that serialVersionUID be present. 
	// In practice, most people rely on the compiler to insert an appropriate UID
	@SuppressWarnings("serial")
	class Folder extends File {
		private boolean isParent;

		public Folder(File file) {
			super(file.getAbsolutePath());
		}

		public Folder(File file, boolean unused) {
			super(file.getParent());
			isParent = true;
		}
		
		public Folder(String path) {
			super(path);
		}
	}
}
