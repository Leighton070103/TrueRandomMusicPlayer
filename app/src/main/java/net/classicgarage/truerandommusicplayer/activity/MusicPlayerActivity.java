package net.classicgarage.truerandommusicplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.model.SongItem;
import net.classicgarage.truerandommusicplayer.service.PlayerService;
import net.classicgarage.truerandommusicplayer.service.PlayerService.PlaybackMode;
import net.classicgarage.truerandommusicplayer.service.PlayerService.PlayerServiceState;

import static net.classicgarage.truerandommusicplayer.util.IntentHelper.getExplicitIntentForService;

public class MusicPlayerActivity extends Activity
	implements OnClickListener{
	//, OnSharedPreferenceChangeListener, SensorEventListener { 
	
	private static final String TAG = "MusicPlayerActivity";
	
	public static final int ALBUM_ART_HEIGHT = 150;
	public static final int ALBUM_ART_WIDTH = 150;
	public static final String REQUEST_CODE="070103";
	
	public static final int REQUEST_PICK_SONG = 0; 	// used for calling SongPicker activity
	
	PlayerStatusReceiver receiver ; 	// broadcast receiver to receive updates about player status

    ImageButton mPlayPauseButton;
    ImageButton mRandomButton;
    ImageButton mSkipButton;
    ImageButton mRewButton;
    ImageButton mStopButton;
	ImageButton mFavoriteButton;
	ImageButton mFilePickerButton;
    ImageButton mPlayListButton;
    TextView mSongTitle;
    ImageView mAlbumArt;
    //MenuTabActivity mMenuTabActivity;
    
    SongItem songPlaying;
    
    private PlayerServiceState mPlayerServiceState = PlayerServiceState.Inexistant;
    private PlaybackMode mPlaybackMode = PlaybackMode.RANDOM;

   
	Resources res ;		// used to access button bitmaps
	   
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.d(TAG, "onCreate");
        
        mSongTitle = (TextView) findViewById(R.id.title_tv);
        mAlbumArt = (ImageView) findViewById(R.id.cover_iv);
        
        mPlayPauseButton = (ImageButton) findViewById(R.id.pause_btn);
        mRandomButton = (ImageButton) findViewById(R.id.random_btn);
        mSkipButton = (ImageButton) findViewById(R.id.next_btn);
        mRewButton = (ImageButton) findViewById(R.id.pre_btn);
        mPlayListButton = (ImageButton) findViewById(R.id.playlist_btn);


		// the below are the buttons that used in the previous xml layout.
        /*mStopButton = (ImageButton) findViewById(R.id.stopbutton);
        mFavoriteButton = (ImageButton) findViewById(R.id.favoritebutton);
        mFilePickerButton = (ImageButton) findViewById(R.id.filepickerbutton);*/

		mPlayPauseButton.setOnClickListener(this);
//		mSkipButton.setOnClickListener(this);
//		mRewButton.setOnClickListener(this);
//		mRandomButton.setOnClickListener(this);
        mPlayListButton.setOnClickListener(this);

		// the below are the buttons that used in the previous xml layout.
		/*mStopButton.setOnClickListener(this);
        mFavoriteButton.setOnClickListener(this);
        mFilePickerButton.setOnClickListener(this);*/
                        
        receiver = new PlayerStatusReceiver();	// broadcast receiver for status updates from PlayerService
        res = getResources();

        // the below manages swipe movements
        //ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this);
        //LinearLayout mainLayout = (LinearLayout)this.findViewById(R.id.mainlayout);
        //mainLayout.setOnTouchListener(activitySwipeDetector);
		/*int code = ActivityCompat.checkSelfPermission(
				this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (code != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		}*/
    }
    
    @Override
    public void onResume() {
    	Log.d(TAG, "on Resume");    	
    	super.onResume();
    	
    	//register to broadcast receiver
    	registerReceiver(receiver, new IntentFilter(PlayerService.NEW_PLAYER_STATE_INTENT));
    	
        // start service if not started
        // if service already running, request a broadcast with song title and player status
    	Intent intent = new Intent(PlayerService.ACTION_BROADCAST);
      	//Intent intent = new Intent(MusicPlayerActivity.this, PlayerService.class);

        intent.putExtra(PlayerService.BROADCAST_REQUEST_SONG_TITLE, true);
        intent.putExtra(PlayerService.BROADCAST_REQUEST_ALBUM_ART, true);
        intent.setPackage(this.getPackageName());
		//Intent eIntent = new Intent(createExplicitFromImplicitIntent(this,intent));
        startService(intent);
    }

    
    @Override
    public void onPause() {
    	Log.d(TAG, "on Pause");
    	super.onPause();
    	
    	// unregister to broadcast receiver
    	unregisterReceiver(receiver);
    	
    	// unregister for accelerometer events
    	//mSensorManager.unregisterListener(this);
    }
    
    @Override
    public void onStop(){
    	Log.d(TAG, "on Stop");    	
    	super.onStop();
    }
    
    public void onClick(View target) {
        // Send the correct intent to the MusicService, according to the button that was clicked

        if (target == mPlayPauseButton) {
        	if (mPlayerServiceState == PlayerServiceState.Playing)
        		startService(getExplicitIntentForService(this,PlayerService.ACTION_PAUSE));

            else
                startService(getExplicitIntentForService(this,PlayerService.ACTION_PLAY));
        // we will update the button bitmap and song title through a broadcast sent by service
        }
        else if (target == mSkipButton)
            startService(getExplicitIntentForService(this,PlayerService.ACTION_SKIP));
        else if (target == mRewButton)
            startService(getExplicitIntentForService(this,PlayerService.ACTION_REW));
        else if (target == mStopButton)
            startService(getExplicitIntentForService(this,PlayerService.ACTION_STOP));
        else if (target == mRandomButton) {
        	// select the next playing mode by rotation R, RF, S
            Intent i = getExplicitIntentForService(this,PlayerService.ACTION_PLAYBACK_MODE);
        	switch (mPlaybackMode) {
        	case RANDOM:
        		if (mPlayerServiceState == PlayerServiceState.Playing 
        				|| mPlayerServiceState == PlayerServiceState.Paused) {
        			i.putExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE, PlayerService.PlaybackMode.RANDOM_FAVORITE);
        			startService(i);
        		}
        		break;
        	case RANDOM_FAVORITE:
        		if (mPlayerServiceState == PlayerServiceState.Playing 
        				|| mPlayerServiceState == PlayerServiceState.Paused) { 
        			i.putExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE, PlayerService.PlaybackMode.SEQUENTIAL);
        			startService(i);
        		}
        		break;        		
        	case SEQUENTIAL:
    			i.putExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE, PlayerService.PlaybackMode.RANDOM);
    			startService(i);
        		break;
        	}        	
        }
        else if (target == mFavoriteButton) {
        	Intent i = new Intent(PlayerService.ACTION_FAVORITE);
        	i.putExtra(PlayerService.INTENT_EXTRA_SONG_FAVORITE, !songPlaying.getFavorite());    			
        	startService(i);
        }
        else if (target == mFilePickerButton) {
        	 Intent i = new Intent(this, SongPickerActivity.class);
        	 if (mPlayerServiceState != PlayerServiceState.Inexistant) {
        		 i.putExtra(PlayerService.INTENT_EXTRA_PLAY_SONG_ID, songPlaying.getId());
        	 }
    	     startActivityForResult(i, REQUEST_PICK_SONG);
        }
        else if (target == mPlayListButton){
            Intent i = new Intent(this,SongListActivity.class);
            startActivity(i);
        }
    }

	/**
	 * update buttons
	 */
	private void updateButtonDisplay () {
		
		if (mPlayerServiceState != PlayerServiceState.Playing) {
			mPlayPauseButton.setBackgroundDrawable(res.getDrawable(R.drawable.btn_play));
			mSongTitle.setTextColor(getResources().getColor(R.color.textDim));
		}
		else {
			mPlayPauseButton.setBackgroundDrawable(res.getDrawable(R.drawable.btn_pause));
			mSongTitle.setTextColor(getResources().getColor(R.color.text));
		}
	
		if (mPlayerServiceState == PlayerServiceState.Inexistant) {
			mSkipButton.setBackgroundDrawable(res.getDrawable(R.mipmap.ff_dim));
			mRewButton.setBackgroundDrawable(res.getDrawable(R.mipmap.rew_dim));
			mStopButton.setBackgroundDrawable(res.getDrawable(R.mipmap.stop_dim));
			mFavoriteButton.setVisibility(View.INVISIBLE);
		}
		else {
			mSkipButton.setBackgroundDrawable(res.getDrawable(R.drawable.btn_ff));
			mStopButton.setBackgroundDrawable(res.getDrawable(R.drawable.btn_stop));
			mRewButton.setBackgroundDrawable(res.getDrawable(R.drawable.btn_rew));		
			mFavoriteButton.setVisibility(View.VISIBLE);
		}

    	switch (mPlaybackMode) {
    	case RANDOM:
			mRandomButton.setBackgroundDrawable(res.getDrawable(R.mipmap.shuffle));
			break;
    	case RANDOM_FAVORITE:
			mRandomButton.setBackgroundDrawable(res.getDrawable(R.mipmap.shuffle_fav));
			break;
    	case SEQUENTIAL:    		
			mRandomButton.setBackgroundDrawable(res.getDrawable(R.mipmap.straight));
			break;
    	}
	}
	
	/*
	 * receive result from SongPickerActivity
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		 Log.d(TAG, "onActivityResult : requestCode="+requestCode+" resultCode="+resultCode);
		 
		super.onActivityResult(requestCode, resultCode, intent);
	        
		if (resultCode == RESULT_CANCELED) return;	       
	    Bundle extras = intent.getExtras();
		    
        if (requestCode == REQUEST_PICK_SONG) {
        	SongItem song = (SongItem) extras.getSerializable(SongPickerActivity.INTENT_EXTRA);
 
   		 	Log.d(TAG, "onActivityResult : song="+song);

        	Intent i = new Intent(PlayerService.ACTION_PLAY);
        	i.putExtra(PlayerService.INTENT_EXTRA_PLAY_SONG_ID, song);    			
        	startService(i);    	
	    }
	 }
	 
	 
    /*
     * inner broadcast receiver class
     */
    class PlayerStatusReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive (Context context, Intent intent) {
    	    		
    		Bitmap albumArt = null;
    		
    		boolean mustUpdateButtons = false ;
    		
    		// player state
    		if (!intent.getAction().equals(PlayerService.NEW_PLAYER_STATE_INTENT)) {
    			Log.i(TAG, "onReceive : got unexpected intent action");
    			return ;
    		}
    		
    		if (intent.hasExtra(PlayerService.INTENT_EXTRA_PLAYER_STATE)) {
	    		mPlayerServiceState = (PlayerServiceState) intent.getSerializableExtra(PlayerService.INTENT_EXTRA_PLAYER_STATE);
	   			Log.d(TAG, "received broadcast for player state changed to " + mPlayerServiceState.toString());   

	   			mustUpdateButtons = true ;
    		}    		


    		// song title
    		if (intent.hasExtra(PlayerService.INTENT_EXTRA_SONG_PLAYING)) {
    			songPlaying = (SongItem) intent.getSerializableExtra(PlayerService.INTENT_EXTRA_SONG_PLAYING);
    			
    			if (songPlaying != null){
	    		    songPlaying.setFavorite(intent.getBooleanExtra(PlayerService.INTENT_EXTRA_SONG_FAVORITE, false));
	    		    
	    		    mSongTitle.setText(songPlaying.toString());
	    		    
	    			Log.d(TAG, "received broadcast for songTitle changed to " + songPlaying.getTitle());
	
	    			if (songPlaying.getFavorite())
	   					mFavoriteButton.setBackgroundDrawable(res.getDrawable(R.mipmap.star_empty));
	   				else
	   					mFavoriteButton.setBackgroundDrawable(res.getDrawable(R.mipmap.star_empty_dim));
    			}
    		}
    		
    		// album art
    		if (intent.hasExtra(PlayerService.INTENT_EXTRA_ALBUM_ART)) {
	    		albumArt = (Bitmap) intent.getParcelableExtra(PlayerService.INTENT_EXTRA_ALBUM_ART);
				Log.d(TAG, "received broadcast for album art changed");
				
				//mAlbumArt.setImageBitmap(albumArt == null ? DummyAlbumArt : albumArt);
				mAlbumArt.setImageBitmap(albumArt);
    		}
    		
    		// Play Mode
    		if (intent.hasExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE)) {
    			mPlaybackMode = (PlaybackMode) intent.getSerializableExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE);    			
       			Log.d(TAG, "received broadcast for playback mode changed to " + mPlaybackMode);          		

    			mustUpdateButtons = true;
    		}

    		// Song catalog
       		if (intent.hasExtra(PlayerService.INTENT_EXTRA_SONGS_CATALOG)) {
       			Toast.makeText(context, "Songs catalog has been rebuilt", Toast.LENGTH_SHORT).show();
       		}
    		
    		if (mustUpdateButtons) 
	    		updateButtonDisplay();	// dim / update buttons layout
    	}
    }      
	
	// start menu first time user press on the "menu" touch
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater ();
    	inflater.inflate(R.menu.menu_main, menu);
    	return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
    	MenuItem itemDelete = menu.getItem(3);
    	if (mPlayerServiceState != PlayerServiceState.Inexistant) {
    		itemDelete.setVisible(true);
    	}
    	else {
    		itemDelete.setVisible(false);
    	}

    	return true;
    }
 
    //handle menu options 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
    	case R.id.preferences:
    		Intent settingsActivity = new Intent(getBaseContext(), PrefsActivity.class);
    		startActivity(settingsActivity);
    		break;
    	case R.id.killService:
    		startService(new Intent(PlayerService.ACTION_KILL));   
    		break;
    	case R.id.rebuildCatalog:
    		startService(new Intent(PlayerService.ACTION_CATALOG_REBUILD));   
    		break;
    	case R.id.deleteSong:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Are you sure you want to delete song '"+songPlaying.toString()+" ?")
 			       .setCancelable(false)
 			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	  startService(new Intent(PlayerService.ACTION_DELETE_SONG));
 			           }
 			       })
 			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			           }
 			       });
 			AlertDialog alert = builder.create();	
 			alert.show();
    		
    	}
		return true;    	
    }
}