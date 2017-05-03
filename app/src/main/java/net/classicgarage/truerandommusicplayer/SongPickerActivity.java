package net.classicgarage.truerandommusicplayer;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * song picker
 */
public class SongPickerActivity extends ListActivity implements OnClickListener {
	public static final String INTENT_EXTRA = "net.classicGarage.FilePicker";
	
	ArrayList<SongItem> mSongsCatalog;
	private ArrayAdapter<SongItem> songSelectorAdapter;
    private boolean onlyFavorites = false;

    private MusicEngine musicEngine;	// ref to application MusicEngine instance
    
	CheckBox cbOnlyFavorites;
	Button bDisplay;
	private MusicEngine mMU;
    
	private static final String TAG = "FilePickerActivity";
	
	private ListView lv;
	private long idSongPlaying = -1;

	private MusicEngine.displayTypes currentDisplayType; 
	private MusicEngine.displayTypes newDisplayType; 

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songpicker);			// to override default ListView layout
        
        this.setTitle("Song Picker");
		cbOnlyFavorites = (CheckBox) findViewById(R.id.checkBoxFavorite);
		bDisplay = (Button) findViewById(R.id.buttonDisplay);
				
		musicEngine = PlayerApplication.getInstance().getMusicEngine();
		currentDisplayType = musicEngine.getCatalogSortOrder();
		newDisplayType = currentDisplayType;
		
		bDisplay.setText((currentDisplayType == MusicEngine.displayTypes.PATHDISPLAY ? "display songs" : "display paths"));
		bDisplay.setOnClickListener(this);

        //get current song if any
        Intent intent = getIntent();
        if (intent.hasExtra(PlayerService.INTENT_EXTRA_PLAY_SONG_ID))  {
			idSongPlaying = intent.getLongExtra(PlayerService.INTENT_EXTRA_PLAY_SONG_ID, -1); 
		} 
   }
   
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "in onPause");       
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        Log.d(TAG, "in onDestroy");
    }
        
    @Override
    protected void onResume() {
        Log.d(TAG, "in onResume");
        super.onResume();

        // get a reference on the app music catalog
		mSongsCatalog = musicEngine.getSongsCatalog();
        
		lv = getListView();	// gets the listview attached to the ListActivity
		
		fillData();
		     
		// remove line divider
		lv.setDivider(null);
		lv.setDividerHeight(0);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {
	            
		    	SongsSelectorAdapter adapter = (SongsSelectorAdapter)parent.getAdapter();
		        SongItem songPickedUp = (SongItem) adapter.getItem(position);		         

		        Log.d (TAG, "OnItemClickListener : song picked up position "+position+" is id="+songPickedUp.getId()+" "+songPickedUp.getArtistTitle());
		        
		    	Bundle bundle = new Bundle();	            
	            // we pass the song in a bundle
	            bundle.putSerializable(INTENT_EXTRA, songPickedUp);
	            Intent mIntent = new Intent();
	            mIntent.putExtras(bundle);
	            setResult(RESULT_OK, mIntent);
	            
	            // update TRMP songs catalog with new sort order
	            //musicEngine.setSongsCatalog(songsCatalog);
	            musicEngine.setCatalogSortOrder(currentDisplayType);
	            
	            finish();	// activity is done	  
		    }
		  });
		
		cbOnlyFavorites.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (buttonView.isChecked()) {
					// restrict songs displayed to favorite songs
					onlyFavorites = true;
				}
				else {
					// no restrictions
					onlyFavorites = false;
				}
				// reload listView
				fillData();
			}
		});

		// display keyboard
//		InputMethodManager inputMgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//		inputMgr.toggleSoftInput(0, 0);
    }
    
    public void onClick(View target) {
 
    	if (target == bDisplay) {
    		// change type
     		newDisplayType = (currentDisplayType == MusicEngine.displayTypes.PATHDISPLAY 
     				? MusicEngine.displayTypes.SONGDISPLAY 
     				:MusicEngine.displayTypes.PATHDISPLAY); 
     		
    		new SortCatalogTask().execute((Void)null);
     		
        }

    }
    
    /**
     * private Thread for sorting catalog
     * @author phid75
     *
     */
    private class SortCatalogTask extends AsyncTask<Void, Void, Void> {
        /** The system calls this to perform work in a worker thread and
          * delivers it the parameters given to AsyncTask.execute() */
        protected Void doInBackground(Void... nothings) {
        	// sort catalog
     		if (newDisplayType.equals(MusicEngine.displayTypes.PATHDISPLAY)) 
     			Collections.sort(mSongsCatalog, new SongItem.PathComparator());
     		else
     			Collections.sort(mSongsCatalog, new SongItem.ArtistAlbumSongsComparator());
            return (Void) null;
        }
        
        /** The system calls this to perform work in the UI thread and delivers
          * the result from doInBackground() */
        protected void onPostExecute(Void result) {
     		// display sorted catalog
    		fillData();
    		
    		// change button text
    		bDisplay.setText((newDisplayType == MusicEngine.displayTypes.PATHDISPLAY ? "display songs" : "display paths"));
    		
       		currentDisplayType = newDisplayType;
            
        }
    }
    
    /*
     * get all entries for category and display them in listView
     */
    private void fillData() {    	
        // Now create an array adapter and set it to display using our row     
    	ArrayList<SongItem> catalog;
    	
    	if (!PlayerApplication.getInstance().isCatalogReady()) {
    		Toast.makeText(this, "Songs catalog is not ready. Please retry in a few sec.", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	if (onlyFavorites) {
    		// filter to retain only favorites
    		catalog = new ArrayList<SongItem>();
    		for (SongItem s:mSongsCatalog) {
    			if (s.getFavorite()) catalog.add(s);
    		}
    	}
    	else {
    			catalog = mSongsCatalog;		// current catalog is already sorted
    	}
    	
        try {
        	songSelectorAdapter = new SongsSelectorAdapter(this,  R.layout.songpicker_row, catalog);

            setListAdapter(songSelectorAdapter);
            //position on current song played
            if (idSongPlaying != -1) {
            	for (int p = 0; p<catalog.size(); p++) {
            		if (catalog.get(p).getId() == idSongPlaying) {
            			lv.setSelection(songSelectorAdapter.getPosition(catalog.get(p)));
            			
            			Log.d(TAG, "fillData : moving to position "+p);
            			break;
            		}
            	}
            }
            
        }
        catch (Exception e)
        {
        	Toast.makeText(this, "could not make it, babe !", Toast.LENGTH_SHORT).show();
        	e.printStackTrace();
        }
    }  

/*
 * private adapter to display a song to pick
 */
private class SongsSelectorAdapter extends ArrayAdapter<SongItem>
implements Filterable{

    private List<SongItem> items;

    public SongsSelectorAdapter(Context context, int textViewResourceId, List<SongItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.songpicker_row, null);
        }

        SongItem thisSong = items.get(position);
        if (thisSong != null) {
            TextView tvSong = (TextView) v.findViewById(R.id.songFile);
            ImageView ivFavorite = (ImageView) v.findViewById(R.id.favoriteSong);
            
            tvSong.setText((currentDisplayType == MusicEngine.displayTypes.SONGDISPLAY ? 
            		thisSong.getArtistTitle() : 
            		thisSong.getPath().replace(PlayerApplication.getInstance().getMusicDirectories().get(0)+File.separatorChar, "")));
            
            //Log.i(TAG, "added song in listview : "+thisSong.getArtistTitle());
            
            if (thisSong.getFavorite())
            	ivFavorite.setVisibility(View.VISIBLE);   
            else
            	ivFavorite.setVisibility(View.INVISIBLE);
        }
        
		return v;
    }
}
}
