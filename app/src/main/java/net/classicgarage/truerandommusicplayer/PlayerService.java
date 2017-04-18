package net.classicgarage.truerandommusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;


public class PlayerService extends Service
implements OnCompletionListener, OnPreparedListener, OnErrorListener, 
	AudioManager.OnAudioFocusChangeListener, SensorEventListener, OnSharedPreferenceChangeListener {

    private final static String TAG = "PlayerService";
    
    private PlayerApplication application;
	private MediaPlayer mPlayer = null;
	private ListIterator<SongItem> mCatalogIterator;
	private SongItem mSong = null;					// current song played
	private ArrayList<SongItem> mPlaybackLog;		// songs played. Used by the REW button
	
	// player state
    enum PlayerServiceState {
        Inexistant,			// MediaPlayer doesn't exist
        Idle,    		// media player has no datasource
        initialized,  	// media player is preparing...
        Prepared,    	// playback ready
        Playing,		// playback active
        Paused,      	// playback paused
        PlaybackComplete	// finished playing song
    };

    private PlayerServiceState mPlayerServiceState = PlayerServiceState.Inexistant;

    
    enum PlaybackMode {
    	RANDOM, 
    	RANDOM_FAVORITE,
    	SEQUENTIAL
    };
    
    private PlaybackMode mPlaybackMode = PlaybackMode.RANDOM;
    
    boolean isPausedByAudioFocusLoss = false; // true if player was paused due to transient audio focus loss 
    
    
    // intent acttions
    public static final String ACTION_NULL = "net.classicgarage.android.musicplayer.action.NULL";
    public static final String ACTION_PLAY = "net.classicgarage.android.musicplayer.action.PLAY";
    public static final String ACTION_PAUSE = "net.classicgarage.android.musicplayer.action.PAUSE";    
    public static final String ACTION_STOP = "net.classicgarage.android.musicplayer.action.STOP";
    public static final String ACTION_SKIP = "net.classicgarage.android.musicplayer.action.SKIP";
    public static final String ACTION_REW = "net.classicgarage.android.musicplayer.action.REW";

    public static final String ACTION_FAVORITE = "net.classicgarage.android.musicplayer.action.FAVORITE";
    public static final String INTENT_EXTRA_SONG_FAVORITE = "songFavorite";	// true/false if favorite status has been set/unset

    public static final String ACTION_PLAYBACK_MODE = "net.classicgarage.android.musicplayer.action.ACTION_PLAYBACK_MODE";
    public static final String ACTION_CATALOG_REBUILD = "net.classicgarage.android.musicplayer.action.CATALOG";
    public static final String ACTION_KILL = "net.classicgarage.android.musicplayer.action.KILL";    
    public static final String ACTION_DELETE_SONG = "net.classicgarage.android.musicplayer.action.DELETE_SONG";    

    public static final String ACTION_BROADCAST = "net.classicgarage.android.musicplayer.action.BROADCAST";
    public static final String BROADCAST_REQUEST_SONG_TITLE = "broadcastSongTitle";	// true if client requests update of song title
    public static final String BROADCAST_REQUEST_ALBUM_ART = "broadcastAlbumArt";	// true if client requests update of album art
   
    // broadcasts
    public static final String NEW_PLAYER_STATE_INTENT = "net.classicgarage.android.musicplayer.new_player_status";
    public static final String INTENT_EXTRA_PLAYER_STATE = "playerState";
    public static final String INTENT_EXTRA_PLAYBACK_MODE = "playbackMode";
    public static final String INTENT_EXTRA_ALBUM_ART = "albumArt";	// album art    
    public static final String INTENT_EXTRA_SONG_PLAYING = "songPlaying";	// the SongItem playing
    public static final String INTENT_EXTRA_SONGS_CATALOG = "songsCatalog";	// the songs catalog
    public static final String INTENT_EXTRA_PLAY_SONG_ID = "songId";	// id of song to play
     
    // handler
    public static int HANDLER_OK = 0;
    public static int HANDLER_ERROR_MSG = 1;
	public static int HANDLER_EMPTY_MSG = 2;
	
    Handler catalogToastHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				int what = message.what;
				
					if (what == HANDLER_OK) {
						Toast.makeText(PlayerService.this, getString(R.string.msg_catalog_rebuilt), Toast.LENGTH_SHORT).show();
					}
					else if (what == HANDLER_ERROR_MSG) {
						Toast.makeText(PlayerService.this, getString(R.string.msg_catalog_rebuild_failed), Toast.LENGTH_SHORT).show();
					}
					else if (what == HANDLER_EMPTY_MSG) {
						Toast.makeText(PlayerService.this, getString(R.string.msg_catalog_empty), Toast.LENGTH_SHORT).show();
					}
				}
			};

    // notifications
    NotificationManager mNotificationManager;
    Notification mNotification;		
    static final int TRMP_NOTIFICATION_ID = 1;
    boolean isForegroundService = false; 	// true if service is foreground
    
    AudioManager mAudioManager;		// used for audio focus

	// used for accelerometer
	private boolean usesShakeToSkip;		// will use feature is set to true
	private boolean usesFaceDownToPause;	// will use feature is set to true
    
	private SensorManager mSensorManager;     
	private Sensor mAccelerometer;
	private long lastTimeSensorChecked;			// last time sensor data were examined
	private long lastTimeIntentStartedBySensor;	// last time a sensor movement led to a playserService intent
    private float x, y, z;
    private float last_x, last_y, last_z;
    private int SHAKE_THRESHOLD;			// speed threshold to skip to next song
    boolean pausedPlaybackByFaceDown = false;		// true if last PAUSE action was made by putting device face down
    
    @Override
    public void onCreate() {
        Log.d(TAG, "Creating service");
        
        application = (PlayerApplication) getApplication();
        
        mPlayerServiceState = PlayerServiceState.Inexistant;    
        
        if (!application.isExternalStorageAvail()) {
        	Toast.makeText(this, "SD card is not accessible. Please solve this issue and retry.", Toast.LENGTH_LONG).show();
    		
    		//end service
    		stopSelf();
        }
        
        Handler handlerMute = new Handler();
        
        Handler handler = catalogToastHandler;
        
        
        // create a new MusicEngine and try to load catalog from disk        
        Log.d(TAG, "onCreate : launching thread in fg to load catalog from disk.");
        Thread t = application.new MusicEngineBuilder(false, handlerMute);
        t.start();
        // for the first catalog load / build, we wait until catalog is ready
        try {
        	t.join();
        }
        catch (InterruptedException e) {};
        
        // if no save copy on disk or pb loading it, we build the catalog afresh
        if (application.getMusicEngine().getSongsCatalogSize() == 0) {
            Log.d(TAG, "onCreate : no catalog on disk. Ask to build a catalog afresh in fg.");
            
            Thread t2 = application.new MusicEngineBuilder(true, handlerMute);
            t2.start();
            // for the first catalog load / build, we wait until catalog is ready
            try {
            	t2.join();
            }
            catch (InterruptedException e) {};        	
        }
        else {        	
        	// saved catalog could be out of date, we rebuild it in a separate thread in bg
        	// when rebuilt, a copy will be saved on disk and current catalog will be replaced by new one
            Log.d(TAG, "onCreate : lazy fresh catalog build.");
            Thread t3 = application.new MusicEngineBuilder(true, handlerMute);
            t3.start();        	
        }
        
        // normally we should have a catalog ready to play
        if (application.getMusicEngine() == null || application.getMusicEngine().getSongsCatalogSize() == 0) {
        	Toast.makeText(this, "no songs in catalog. Aborting.", Toast.LENGTH_SHORT).show();
		
			//end service
			stopSelf();
        }
        
        Log.d(TAG, "onCreate : catalog is ready.");
        
        // initialize songs log
        mPlaybackLog = new ArrayList<SongItem>();
        
        // gets a reference to notification manager
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);  
        
        //accelerometer management
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);         
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);     
        
        // Get preferences from XML file - register for pref changes
    	SharedPreferences prefs ;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        usesShakeToSkip = prefs.getBoolean(PlayerApplication.PREF_SHAKETOSKIP, false);
        SHAKE_THRESHOLD = Integer.parseInt(prefs.getString(PlayerApplication.PREF_SHAKETHRESHOLD, "400"));
        usesFaceDownToPause = prefs.getBoolean(PlayerApplication.PREF_FACEDOWNTOPAUSE, false);
        
        lastTimeSensorChecked = System.currentTimeMillis();
        lastTimeIntentStartedBySensor = System.currentTimeMillis();
        
        Log.d(TAG, "onCreate: Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

    	String action = intent.getAction();
    	
    	Log.d(TAG, "onStartCommand : action="+action.toString());
    	
        if (!application.isExternalStorageAvail()) {
        	Toast.makeText(this, "SD card is not accessible. Please solve this issue and retry", Toast.LENGTH_LONG).show();
        	return START_NOT_STICKY;
        }
 
        if (application.getMusicEngine().getSongsCatalogSize() == 0 // catalog is empty. Should not happen, except if app was first launched through widget !!!
        		&& (
        				action.equals(ACTION_PLAY) ||
        				action.equals(ACTION_REW) ||
        				action.equals(ACTION_SKIP)
        			) 
        	) {
        	// attempt to rebuild catalog
        	Log.e(TAG, "catalog is empty. Attempting to rebuild it");
        	final Handler handler = catalogToastHandler;

        	Thread t = application.new MusicEngineBuilder(true, handler);
        	t.start();
        	try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			;
			}
        }
         
    	// intent to launch service
        if (action.equals(ACTION_NULL)){	// do nothing, just here to launch service
            return START_NOT_STICKY; // but don't want the service to restart in case it's killed.        	
        }

        // user intents
        if (!application.isCatalogReady() && action.equals(ACTION_PLAY)) {
	       	Toast.makeText(this, "catalog build in progress. Please retry in a few seconds" , Toast.LENGTH_SHORT).show();
	        return START_NOT_STICKY; // but don't want the service to restart in case it's killed.
	    }
        
        if (action.equals(ACTION_PAUSE)) 
        	processPauseRequest();	        
        else if (action.equals(ACTION_PLAY)) {
        	SongItem songToPlay = null;
        	if (intent.hasExtra(INTENT_EXTRA_PLAY_SONG_ID))  {
        		songToPlay = (SongItem)intent.getSerializableExtra(INTENT_EXTRA_PLAY_SONG_ID); 	// song to play has been picked up
        		
        		Log.d(TAG, "onStartCommand : songToPlay="+songToPlay);
        	}        
        	processPlayRequest(songToPlay);	        
        }
        else if (action.equals(ACTION_SKIP)) 
        	processSkipRequest();
        else if (action.equals(ACTION_REW)) 
        	processRewRequest();
        else if (action.equals(ACTION_STOP)) 
        	processStopRequest();
        else if (action.equals(ACTION_PLAYBACK_MODE)) {
        	if (intent.hasExtra(INTENT_EXTRA_PLAYBACK_MODE))  {
        		mPlaybackMode  = (PlaybackMode) intent.getSerializableExtra(INTENT_EXTRA_PLAYBACK_MODE);
        		Log.i(TAG, "Intent ACTION_PLAYBACK_MODE has extra : "+mPlaybackMode);
        		switch (mPlaybackMode) {
        		case RANDOM:         		
	        		processRandomPlayModeRequest();
	        		break;
        		case RANDOM_FAVORITE:
        			processRandomFavoritePlayModeRequest();
	        		break;
        		case SEQUENTIAL:
        			processSequentialPlayModeRequest();
        			break;
        		}
        	}
        }
        else if (action.equals(ACTION_CATALOG_REBUILD)) 
        	processCatalogRebuildRequest(true);
        else if (action.equals(ACTION_DELETE_SONG)) 
        	processDeleteSongRequest();
        else if (action.equals(ACTION_BROADCAST)) {
        	boolean bSongTitle = false;
        	boolean bAlbumArt = false;

       
    		if (intent.hasExtra(PlayerService.BROADCAST_REQUEST_SONG_TITLE)) 
    			bSongTitle = intent.getBooleanExtra(BROADCAST_REQUEST_SONG_TITLE, false);
    		if (intent.hasExtra(PlayerService.BROADCAST_REQUEST_ALBUM_ART)) 
    			bAlbumArt = intent.getBooleanExtra(BROADCAST_REQUEST_ALBUM_ART, false);
 
    		broadcastToActivity(bSongTitle, bAlbumArt);
        }
        else if (action.equals(ACTION_KILL)) 
        	processKillRequest();
        else if (action.equals(ACTION_FAVORITE)) 
        	processFavoriteSongRequest(intent.getBooleanExtra(INTENT_EXTRA_SONG_FAVORITE, false));

        return START_NOT_STICKY; // but don't want the service to restart in case it's killed.
    }

    @Override
    public void onDestroy() {
    	// save favorite list to disk
    	application.getMusicEngine().saveFavoritesToDisk();
    	
        // Service is being killed, so make sure we release our resources
        relaxResources();
//        giveUpAudioFocus();
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}


	/**
     * we use a single instance of the media player
     */
    void createOrResetMediaPlayer() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            // register to callbacks
            mPlayer.setOnPreparedListener(this);	// when mediaPlayer is prepared
            mPlayer.setOnCompletionListener(this);	// when song has finished playing            
            mPlayer.setOnErrorListener(this);		// error handling
            
            Log.d(TAG, "Player created");
        }  
        else {
        	mPlayer.reset();
        	
            Log.d(TAG, "Player has been reset");
        }
        mPlayerServiceState = PlayerServiceState.Idle;
    }
    
    /**
     * starts playback
     */
    void startPlayback() {

        mPlayer.setVolume(1.0f, 1.0f); // we can be loud
            
        Log.d(TAG, "starting Media PLayer playback for "+ mSong.toString());            

        try {
        	mPlayer.start();
//        	if (mPlayerServiceState != PlayerServiceState.Paused && 
//        			mPlayerServiceState != PlayerServiceState.PlaybackComplete	)
            mPlayerServiceState = PlayerServiceState.Playing;
        	isPausedByAudioFocusLoss = false;
            
        	broadcastToActivity(true, true);	// sends a broadcast with updated song title and player status
            
            // update notification with song title			
			setUpAsForegroundService(false, mSong.getNotificationText());
			
			// adds song to playback log
			mPlaybackLog.add(mSong);
			Log.d(TAG, "playback log size is now "+mPlaybackLog.size());
			
			if (usesShakeToSkip || usesFaceDownToPause) {
	            // register for accelerometer events
	    		Log.d(TAG, "registring from sensor events");
	    		
	            if (!mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL))
	            	Log.e(TAG, "unable to register listener for accelerometer");
			}
			pausedPlaybackByFaceDown = false;	// indicator reset			
        }
        catch (IllegalStateException ex){
        	Log.e (TAG, "illegal state exception while starting Player"); 
        	ex.printStackTrace();
        }
    }
    
/**
 * sends a broadcast about current player state and song playing
 * @param broadcastSongTitle : if true will broadcast current song
 * @param broadcastAlbumArt : if true will broadcast current song album art
 */
    void broadcastToActivity(boolean broadcastSongTitle, boolean broadcastAlbumArt){
        // we send broadcast to notify of the new status
    	
        Log.d (TAG, "sending Broadcast");
        
        Intent intent = new Intent(NEW_PLAYER_STATE_INTENT);
        
        intent.putExtra(INTENT_EXTRA_PLAYER_STATE,(Serializable)mPlayerServiceState);
        
        if (broadcastSongTitle) {
        	intent.putExtra(INTENT_EXTRA_SONG_PLAYING, mSong);
        	intent.putExtra(INTENT_EXTRA_SONG_FAVORITE, (mSong == null) ? false : mSong.getFavorite());
        }
        
        if (broadcastAlbumArt)
        	intent.putExtra(INTENT_EXTRA_ALBUM_ART, (mSong == null) ? null : application.getMusicEngine().getAlbumArt(mSong.getAlbumId(), 
        		MusicPlayerActivity.ALBUM_ART_HEIGHT, 
        		MusicPlayerActivity.ALBUM_ART_WIDTH));
              
        intent.putExtra(INTENT_EXTRA_PLAYBACK_MODE, mPlaybackMode);
        
        // cannot use localBroadcastManager because of widget
        sendBroadcast(intent);
    
    }      


    /*
     * Pause playback and broadcast about new status
     */
    void pausePlayback() {
        Log.d(TAG, "pausing Media PLayer playback");            

        try {
        	mPlayer.pause();
            mPlayerServiceState = PlayerServiceState.Paused;
            
            // we send broadcast to notify of the new status
            broadcastToActivity(false, false);
            
            // update notification 
            updateNotification(true, mSong.getNotificationText());
        }
        catch (IllegalStateException ex){
        	Log.e (TAG, "illegal state exception will pausing Player");        	
        }
    }
    
/**
 * process a PLAY intent
 * @param songToPlay : if not null, song to play, otherwise will pick up a random song
 */
    void processPlayRequest(SongItem songToPlay) {  
		if (application.getMusicEngine().getSongsCatalogSize() == 0) {
			// attempt to rebuild catalog
			
			Toast.makeText(this, getString(R.string.msg_catalog_empty), Toast.LENGTH_LONG).show();
			return;
		}
		
		/*
		 * activity didn't send a song to play (picked up)
		 */
		if (songToPlay == null) {	
			switch (mPlayerServiceState) {
			case Inexistant: // service was just started
				// request audio focus
				int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
						AudioManager.AUDIOFOCUS_GAIN);

				if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
					// could not get audio focus.
					Toast.makeText (getApplicationContext(), "could not get audio focus. Please retry in a few sec.", 
							Toast.LENGTH_SHORT).show();
				}
				else {
					createOrResetMediaPlayer();	// creates an idle player

					// play song
					preparePlayerForNextSong(); 

					// playback will start when mediaPlayer is prepared through onPrepared()    			
				}            
				break;    		
			case Paused:
				Log.d(TAG, "resuming playback");
				startPlayback();
				break;
			default:
				Log.e(TAG, "processPlayRequest : unhandled player state "+mPlayerServiceState);
				Log.d(TAG, "resetting player");
				// play song
				preparePlayerForNextSong(); 
				break;	    	
			}
		}
		else {
			/*
			 * intent comes with a song to play (picked up by user)
			 */									
	    	if (mPlayerServiceState == PlayerServiceState.Playing
					|| mPlayerServiceState == PlayerServiceState.Paused
					|| mPlayerServiceState == PlayerServiceState.PlaybackComplete) {
	    		createOrResetMediaPlayer(); // reset player to put it in Idle state
	    	}
	    	
	    	mSong = application.getMusicEngine().getSongAt(songToPlay.getId());
			if (!preparePlayerForSong(mSong.getURI())) {
				Toast.makeText(this, "Song cannot be played, the android media player returned an error" , Toast.LENGTH_LONG).show();
			}
   			// playback will start when mediaPlayer is prepared through onPrepared()    			
			 				
    		//if mode straight, update the iterator on the current song
			if (mPlaybackMode == PlaybackMode.SEQUENTIAL)
				processSequentialPlayModeRequest();
			
			Log.d(TAG, "processPlayRequest : processed request for song "+mSong);
			
		}
    }

    /*
     * process a PAUSE intent
     */
    void processPauseRequest() {  
    	if (mPlayerServiceState == PlayerServiceState.Playing) {   	
            Log.d(TAG, "Pausing player");
       		pausePlayback();
    	}
    	else
    		Log.d(TAG, "processPauseRequest : invalid player state "+mPlayerServiceState);
    }

    /*
     * process a SKIP intent
     */
    void processSkipRequest() {
    	if (mPlayerServiceState == PlayerServiceState.Playing
				|| mPlayerServiceState == PlayerServiceState.Paused
				|| mPlayerServiceState == PlayerServiceState.PlaybackComplete) {
    		createOrResetMediaPlayer(); // reset player to put it in Idle state
    	}
    	
		if (application.getMusicEngine().getSongsCatalogSize() > 0 && mPlayerServiceState == PlayerServiceState.Idle) {
	    	// play next song
			preparePlayerForNextSong();
			// playback will start when mediaPlayer is prepared through onPrepared()
		}
    }
    
    /*
     * process a REW intent
     */
    void processRewRequest() {
    	
    	if (mPlaybackLog.size() < 2) {
    		return;		// do nothing
    	}
    	
    	if (mPlayerServiceState == PlayerServiceState.Playing
				|| mPlayerServiceState == PlayerServiceState.Paused
				|| mPlayerServiceState == PlayerServiceState.PlaybackComplete) {
    		createOrResetMediaPlayer(); // reset player to put it in Idle state
    	}
    	
		if (mPlayerServiceState == PlayerServiceState.Idle) {
	    	// play song
			preparePlayerForPreviousSong();
			// playback will start when mediaPlayer is prepared through onPrepared()
		}    	
		
    	Log.d(TAG, "REW request processed.");
    }
    
    /*
     * process a STOP intent
     */
    void processStopRequest() {

    	relaxResources();   // release media player	and puts back service in bg
        mSong = null;
                
        // we send broadcast to notify of the new status
        broadcastToActivity(true, true);
        
        Log.d(TAG, "Stop Request processed");        
        
            /*
            // service is no longer necessary. Will be started again if needed.
            Log.d(TAG, "Stopping service");
            stopSelf();    

 */
    }

    /*
     * process a ACTION_PLAYBACK intent with RANDOM extra
     */
    void processRandomPlayModeRequest() {
    	mPlaybackMode = PlaybackMode.RANDOM ;
    	mCatalogIterator = null;

    	// we send broadcast to notify of the new status
        broadcastToActivity(false, false);
        
        Log.d(TAG, "RandomPlayMode Request processed");  	 	
    }

    /*
     * process a ACTION_PLAYBACK intent with RANDOM_FAVORITE extra
     */
    void processRandomFavoritePlayModeRequest() {
    	mPlaybackMode = PlaybackMode.RANDOM_FAVORITE ;
    	mCatalogIterator = null;

    	// we send broadcast to notify of the new status
        broadcastToActivity(false, false);
        
        Log.d(TAG, "RandomFavoritePlayMode Request processed");  	 	
    }
    
    /*
     * process a ACTION_PLAYBACK intent with SEQUENTIAL extra
     */
    void processSequentialPlayModeRequest() {
    	mPlaybackMode = PlaybackMode.SEQUENTIAL ;
    	
    	// we need to initialize an iterator for sequential browsing of the catalog
    	int currentIndex = (mSong != null  ? application.getMusicEngine().getSongsCatalog().indexOf(mSong) : 0); 
    	if (currentIndex == application.getMusicEngine().getSongsCatalogSize()-1) 
    		currentIndex = -1;
    	
    	mCatalogIterator = application.getMusicEngine().getSongsCatalogIterator(mSong);

    	// we send broadcast to notify of the new status
        broadcastToActivity(false, false);    	
   	 	
        Log.d(TAG, "SequentialPlayMode Request processed. index = "+ Integer.toString(currentIndex)); 
    }
    
/**
 * process a ACTION_SONG_FAVORITE intent
 * @param status : true if song to add to favorites, false otherwise
 */
    void processFavoriteSongRequest(boolean status) {
    	// add favorite status to song in catalog
    	mSong.setFavorite(status);
    	
    	// update favorites list
    	application.getMusicEngine().setFavorite(mSong.getKey(), status);
    	if (status)
    		Toast.makeText(this, getString(R.string.msg_added_to_favorites), Toast.LENGTH_SHORT).show();
    	else
    		Toast.makeText(this, getString(R.string.msg_removed_from_favorites), Toast.LENGTH_SHORT).show();
    	
    	// save favorite list to disk
    	application.getMusicEngine().saveFavoritesToDisk();
    	
    	// we send broadcast to notify of the new status
        broadcastToActivity(true, false);    	
   	 	
        Log.d(TAG, "favorite status of song"+mSong.getKey()+" updated."); 
    }
    
    /*
     * process a ACTION_SEQUENTIAL_PLAY_MODE intent
     */
    void processKillRequest() {
    	
    	processStopRequest();
    	
       	Toast.makeText(this, "Player has been reset", Toast.LENGTH_SHORT).show();
    	
		//end service
		stopSelf();
    }
    
/**
 * process a ACTION_CATALOG intent
 * @param toast : obsolete
 */
    void processCatalogRebuildRequest(boolean toast) {
    	 Log.d(TAG, "processCatalogRequest intent : lazy fresh catalog build. Toast="+toast);
    	 
    	 final Handler handler = catalogToastHandler;

         Thread t = application.new MusicEngineBuilder(true, handler);
         t.start();            
    }

    /*
     * process a ACTION_SEQUENTIAL_PLAY_MODE intent
     */
    void processDeleteSongRequest() {
    	SongItem nextSong = null;
    	
    	Log.i(TAG, "processDeleteSongRequest : start");
    	
    	// memorize next song 
    	if (mPlaybackMode.equals(PlaybackMode.SEQUENTIAL))
    		nextSong = application.getMusicEngine().getNextSong(mCatalogIterator);
    	
    	// delete song from SD card
    	if (application.getMusicEngine().deleteSong(mSong) ==1) {
    		Log.d(TAG, "processDeleteSongRequest : song has been deleted");
    		Toast.makeText(this, "song has been deleted", Toast.LENGTH_SHORT).show();  
    		
			// create new catalog iterator since catalog was modified
        	if (mPlaybackMode.equals(PlaybackMode.SEQUENTIAL))
        		mCatalogIterator = application.getMusicEngine().getSongsCatalogIterator(nextSong);
    		
			// skip to next song
    		processSkipRequest();
    	}
    	else {
    		Log.e(TAG, "processDeleteSongRequest : song was NOT deleted");
    	}
    }
    /*
     * prepares player for next song 
     */   
    void preparePlayerForNextSong() {   
    	int retry = 0;
    	
    	// in case of media prepare error, we retry one time
    	while (retry <=1) {
    		switch (mPlaybackMode) {
    		case RANDOM:
    			mSong = application.getMusicEngine().getRandomSong();
    			break;
    		case RANDOM_FAVORITE:
    			mSong = application.getMusicEngine().getRandomFavoriteSong();
    			if (mSong == null) {	// no more favorite songs
    				Toast.makeText(this, "No favorite songs !", Toast.LENGTH_SHORT).show();
    				mSong = application.getMusicEngine().getRandomSong();
    			}
    			break;
    		case SEQUENTIAL:
    			if (!mCatalogIterator.hasNext()) {	// we are at the end of the playlist. 
    				// resetting iterator
    				mCatalogIterator = application.getMusicEngine().getSongsCatalogIterator(null);
    			}
    			mSong = application.getMusicEngine().getNextSong(mCatalogIterator);
    			break;
    		}

    		// play song
    		if (!preparePlayerForSong(mSong.getURI())) {
    			Log.d(TAG, "preparePlayerForNextSong : prepare failed. Incrementing retry counter");
    			retry++;
    		}
    		else {
    			// we step out the retry loop
    			Log.d(TAG, "preparePlayerForNextSong : prepare succeeded.");

    			break;
    		}
    	}
    }

    /*
     * prepares player for previous song 
     */   
    void preparePlayerForPreviousSong() {
    	if (mPlaybackLog.size()<2)
    		return;
    	
    	// get the last song from the play log    	
    	mPlaybackLog.remove(mPlaybackLog.size()-1);
    	mSong = mPlaybackLog.remove(mPlaybackLog.size()-1); // remove it coz it will be added by playback method
    	
    	switch (mPlaybackMode) {
    	case RANDOM:
    	case RANDOM_FAVORITE:
    		break;
    	case SEQUENTIAL:
    		if (mCatalogIterator.hasPrevious()) {	 
    			// resetting iterator
    			mCatalogIterator.previous();
    		}
    		break;
    	}

    	// play song. there shouldn't be any problem since we already played this one
		preparePlayerForSong(mSong.getURI());
    }

	
/**
 * prepares player for song specified in URI
 */
    boolean preparePlayerForSong(Uri songUri) {
    	boolean result = false;
    	
	   	if (!application.isExternalStorageAvail()) {
	      	Toast.makeText(this, "SD card is not accessible. Please solve this issue and retry.", Toast.LENGTH_LONG).show();
	      	return false;
	 	}
	   	
    	// put player in Idle state if necessary (skip request or playback completed for instance) 
        if (mPlayerServiceState != PlayerServiceState.Idle) createOrResetMediaPlayer();
        
        //sets the URI of song to play
        try {
	        Log.d(TAG, "setup DataSource for URI " + songUri);   
	        
			mPlayer.setDataSource(getApplicationContext(), songUri);
			mPlayerServiceState = PlayerServiceState.initialized;
			
			// preparing player. When prepared, onPrepared() will be called
	        Log.d(TAG, "preparing player");
	        mPlayer.prepareAsync();
	        
	        result = true;
		} catch (IOException ex) {
            Log.e(TAG, "IOException while setting up datasource: " + ex.getMessage());
            ex.printStackTrace();
        }   
        return result;
    }


    /**
     * Configures service as a foreground service. 
     */
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	void setUpAsForegroundService(boolean isPaused, String songTitle) {
    	if (!isForegroundService) {
    		//TODO is there a way to test if service is fg or bg
    		
    		Intent i = new Intent(getApplicationContext(), MusicPlayerActivity.class);
    		// we don't need to put extras in the intent, song title and status will be retrieved by a service broadcast

	        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
	                i, PendingIntent.FLAG_CANCEL_CURRENT);
	        
	        mNotification = new Notification();
	        mNotification.icon = R.mipmap.ic_stat_playing;
	        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
	        //mNotification.flags |= Notification.FLAG_AUTO_CANCEL;	
	        
	        //mNotification.setLatestEventInfo(getApplicationContext(), getResources().getText(R.string.app_title),
	        //        songTitle, pi);   //Now it is replaced by notification.builder, show as below.


			mNotification = new Notification.Builder(getApplicationContext())
					.setContentTitle(getResources().getString(R.string.app_title))
					.setContentText(songTitle)
					.build();
	        
	        Log.d(TAG, "starting service in foreground");
	        startForeground(TRMP_NOTIFICATION_ID, mNotification);
	        isForegroundService = true;
    	}
    	else {
    		updateNotification(isPaused, songTitle);
    	}
    }

    /** Updates the notification. */
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	void updateNotification(boolean isPaused, String songTitle) {
		Intent i = new Intent(getApplicationContext(), MusicPlayerActivity.class);
		// we don't need to put extras in the intent, song title and status will be retrieved by a service broadcast

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                i, PendingIntent.FLAG_CANCEL_CURRENT);

        String text = songTitle + (isPaused ? "(paused)":""); 
        //mNotification.setLatestEventInfo(getApplicationContext(), getResources().getText(R.string.app_title), text, pi);// To old to be used

		mNotification = new Notification.Builder(getApplicationContext())
				.setContentTitle(getResources().getString(R.string.app_title))
				.setContentText(text)
				.build();
        
        Log.d(TAG, "updating notification");
        
        mNotificationManager.notify(TRMP_NOTIFICATION_ID, mNotification);

    }
    

    void relaxResources() {
        // stop being a foreground service
        stopForeground(true);
        mNotificationManager.cancel(TRMP_NOTIFICATION_ID);	// cancel TRMP notification in status bar

        // stop and release the Media Player, if it's available
        
        if (mPlayer != null) {
        	try {
        		if (mPlayer.isPlaying()) mPlayer.stop();
        	}
        	catch (IllegalStateException e) {
        		Log.e(TAG, "releaseResources : Illegal StateException");
        		e.printStackTrace();
        	}
            mPlayer.release();
            mPlayer = null;
        	
            mPlayerServiceState = PlayerServiceState.Inexistant;
            
            mAudioManager.abandonAudioFocus(this);	           
        }
        
    	isPausedByAudioFocusLoss = false;  // used for audio focus change 
    	
    	mSensorManager.unregisterListener(this);	// unregister accelerometer events
    }
    
/**
 * Called when media player is done playing current song.
 * @param player : media player    
 */
    public void onCompletion(MediaPlayer player) {
        // play next song
    	mPlayerServiceState = PlayerServiceState.PlaybackComplete;
		createOrResetMediaPlayer(); // reset player to put it in Idle state

    	// play next song
		preparePlayerForNextSong();
		// playback will start when mediaPlayer is prepared through onPrepared()
    }
    
    /**
     * Called when media player is done preparing.
     * @param player : media player    
     */
    public void onPrepared(MediaPlayer player) {
        // The media player is done preparing. That means we can start playing!
    	mPlayerServiceState = PlayerServiceState.Prepared;
    	
//        updateNotification(mSong.getNotificationText()+" (playing)");
        startPlayback();
    }
    
    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
            Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

//        mState = State.Stopped;
        relaxResources(); 
      
//        giveUpAudioFocus();
        return true; // true indicates we handled the error
    }
    
    
    /**
     * Preference Management
     */
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(TAG, "preference changed : key="+key);
		
		if (key.equals(PlayerApplication.PREF_SHAKETOSKIP)) {
			usesShakeToSkip = prefs.getBoolean(key, usesShakeToSkip);
			Log.d(TAG, "preference new value : key="+usesShakeToSkip);
		}
		else if (key.equals(PlayerApplication.PREF_SHAKETHRESHOLD)) {
	        SHAKE_THRESHOLD = Integer.parseInt(prefs.getString("shakeThreshold", "400"));
		}
		else if (key.equals(PlayerApplication.PREF_FACEDOWNTOPAUSE)) {
			usesFaceDownToPause = prefs.getBoolean(key, usesFaceDownToPause);
			Log.d(TAG, "preference new value : key="+usesFaceDownToPause);
		}
		
    	if (!usesShakeToSkip && !usesFaceDownToPause) {
    		mSensorManager.unregisterListener(this);	    	// unregister for accelerometer events
    	}    	
	}

    /*
     * (non-Javadoc)
     * @see android.media.AudioManager.OnAudioFocusChangeListener#onAudioFocusChange(int)
     */
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback if player was paused
            	Log.d(TAG, "audio focus change : AUDIOFOCUS_GAIN");
            	
            	// we will resume playback only if paused state wias due to an audio focus transceint loss
            	if (mPlayerServiceState == PlayerServiceState.Paused && 
            			isPausedByAudioFocusLoss)
            		startPlayback();
            	else            	
            		mPlayer.setVolume(1.0f, 1.0f);		// we put back volume to full level in case it was docked
            	
            	isPausedByAudioFocusLoss = false;
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
            	Log.d(TAG, "audio focus change : AUDIOFOCUS_LOSS");
            	
            	processStopRequest();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
            	Log.d(TAG, "audio focus change : AUDIOFOCUS_LOSS_TRANSIENT");
            	
            	if (mPlayerServiceState == PlayerServiceState.Playing) {
            		pausePlayback();            		
                    isPausedByAudioFocusLoss = true;
            	}
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
            	Log.d(TAG, "audio focus change : AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            	
                if (mPlayer.isPlaying()) mPlayer.setVolume(0.1f, 0.1f);                
                break;
        }
            	
    }
    
    /**
     * sensor management
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {     
    	
    }     
    
    public void onSensorChanged(SensorEvent event) {
    	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
    	    long curTime = System.currentTimeMillis();
    	    
    	    if ((curTime - lastTimeSensorChecked) > 100	// only allow one update every 100ms.
    	    		&& (curTime - lastTimeIntentStartedBySensor > 1000)  // max one intent per second
    	    		) {
	    		long diffTime = (curTime - lastTimeSensorChecked);
	    		lastTimeSensorChecked = curTime;
	     
	    		x = event.values[SensorManager.DATA_X];
	    		y = event.values[SensorManager.DATA_Y];
	    		z = event.values[SensorManager.DATA_Z];
	    		
	    		//Log.v(TAG, String.format("x=%f, y=%f, z=%f  lastx=%f lasty=%f lastz=%f", x, y, z, last_x, last_y, last_z));
	     
	    		// check for shaking - we don't include z in speed computation
	    		float speed = Math.abs(x+y - last_x - last_y) / diffTime * 10000;
	    		
	    		if (usesShakeToSkip && speed > SHAKE_THRESHOLD) {
	    			Log.d(TAG, "shake speed exeeds threshold. launching SKIP request");
	    			processSkipRequest();
	    			lastTimeIntentStartedBySensor = curTime;
	    		}

	    		// check for face up or down
	            if (usesFaceDownToPause) {	
	            	if (mPlayerServiceState == PlayerServiceState.Paused &&
	            		z >9 && z < 11 && !(last_z >9 && last_z <11) &&
	            		pausedPlaybackByFaceDown) {	// if paused by PAUSE button, we won't let it unpause by face up
		            	Log.d(TAG, "onSensorChanged : launching Pause request");
		                processPlayRequest(null);
		                lastTimeIntentStartedBySensor = curTime;
		            }
		            else if (mPlayerServiceState == PlayerServiceState.Playing
		            		&& z > -11 && z < -9 && !(last_z>-11 && last_z< -9)) {
		            	Log.d(TAG, "onSensorChanged : launching Pause request");
		                processPauseRequest();	
		                pausedPlaybackByFaceDown = true;
		                lastTimeIntentStartedBySensor = curTime;
		            }
	            }

	    		last_x = x;
	    		last_y = y;
	    		last_z = z;
	        }
    	}
    }

}
    
	
	