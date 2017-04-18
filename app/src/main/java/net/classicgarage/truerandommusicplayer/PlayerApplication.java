package net.classicgarage.truerandommusicplayer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class PlayerApplication extends Application 
implements OnSharedPreferenceChangeListener {
	
	private File mRootSDCard;						// SD Card mounting point
	private ArrayList<String> mMusicDirectories;		// location of music files on disk
	private static Semaphore mfileAccessSemaphore;	// to prevent concurrent loading and saving of catalog to disk 
	private SharedPreferences mPreferences ;

	private MusicEngine mMusicEngine = null;
	
	private boolean isCatalogReady = false;
    private boolean isCatalogUpToDate = false;
	
	private static final String TAG="PlayerApplication";
	private static PlayerApplication instance;
	
    // preference keys 
    public static final String PREF_SHAKETOSKIP="shakeToSkip";
    public static final String PREF_SHAKETHRESHOLD="shakeThreshold";
    public static final String PREF_FACEDOWNTOPAUSE="faceDownToPause";
    public static final String PREF_MUSICDIRECTORIES="musicDirectories";
	
    @Override
    public void onCreate() {
      super.onCreate(); 
      instance = this;      
      initializeInstance();
    }

    public static PlayerApplication getInstance(){
    	return instance;
    }

	public SharedPreferences getPreferences() {
		return mPreferences;
	}
	
    public File getRootSDCard() {
		return mRootSDCard;
	}

	public MusicEngine getMusicEngine() {
		return mMusicEngine;
	}
	
	public ArrayList<String> getMusicDirectories() {
		return mMusicDirectories;
	}

	public boolean isCatalogReady() {
		return isCatalogReady;
	}

	public boolean isCatalogUpToDate() {
		return isCatalogUpToDate;
	}
	
    /**
     * application singleton initialization code
     */
	protected void initializeInstance() {
        // do all your initialization here
    	if (Environment.getExternalStorageDirectory().canRead()) {
    		mRootSDCard = Environment.getExternalStorageDirectory();
    	}
		else {
			Toast.makeText(this.getApplicationContext(), "SD card appears to be unavailable. Please fix the problem and retry.", Toast.LENGTH_SHORT).show();
			mRootSDCard = null;
		}
    	
        // build catalog using root directories given in preferences

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        
        mMusicDirectories = new ArrayList<String>();
        String musicDirectory = mPreferences.getString(PREF_MUSICDIRECTORIES, Environment.getExternalStorageDirectory().getAbsolutePath());
        File dir = new File(musicDirectory);
        if(!dir.exists() || !dir.isDirectory()) {
        	Log.e(TAG, "Music directory "+musicDirectory+" doesn't exist!");
        	Toast.makeText(this, "directory "+musicDirectory+" doesn't exist!", Toast.LENGTH_LONG).show();	
        	//end service
        }
        mMusicDirectories.add(musicDirectory);       
        Log.d(TAG, "mMusicDirectory="+musicDirectory);
                   
        mfileAccessSemaphore = new Semaphore(1);	// initialize semaphore for file access to catalog copy on disk
        
        // force a rescan of SD card
        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }

	/*
	 * isExternalStorageAvail
	 * true if SD card available
	 */
	public boolean isExternalStorageAvail() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state) || (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
		    // We can read the media
			return true;
		} else {
		    return false;
		}
	}
	
    /*
     * Preference Change
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
     */
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(TAG, "onSharedPreferenceChanged. key="+key);
		if (key.equals(PREF_MUSICDIRECTORIES)) {

	        String musicDirectory = prefs.getString(PREF_MUSICDIRECTORIES, mRootSDCard.getAbsolutePath());
	        File dir = new File(musicDirectory);
	        if(!dir.exists() || !dir.isDirectory()) {
	        	Log.e(TAG, "Music directory "+musicDirectory+" doesn't exist!");
	        	Toast.makeText(this, "directory "+musicDirectory+" doesn't exist!", Toast.LENGTH_LONG).show();	    		
	        }
	        else {
	        	Log.d(TAG, "onSharedPreferenceChanged. key="+key);
		        mMusicDirectories.clear();
	        	mMusicDirectories.add(musicDirectory);
	        }
	        
	        if (!isExternalStorageAvail()) {
	        	Toast.makeText(this, "SD card is not accessible. Please solve this issue and restart service.", Toast.LENGTH_LONG).show();
	        }


	        // build a new catalog in a separate thread
        	// when rebuilt, a copy will be saved on disk and current catalog will be replaced by new one
//            Thread t3 = new MusicEngineBuilder(true);
//            t3.start();  
            startService(new Intent(PlayerService.ACTION_CATALOG_REBUILD));   
		}
	}
	
	 /*
	  * saves an object to disk
	  */
	 public synchronized boolean saveObjectToDisk(String file, Object o){
		boolean success = false;
		ObjectOutputStream os = null;
		
		if (o == null) {
			Log.d (TAG, "saveObjectToDisk : saving failed. Object to save is null.");
			return false;
		}
		
		Log.d (TAG, "saveObjectToDisk : saving ...");
		try {
			mfileAccessSemaphore.acquire();
			
			FileOutputStream fos = openFileOutput(file, Context.MODE_PRIVATE);
			os = new ObjectOutputStream(fos);
			os.writeObject(o);
			success = true;
			
			Log.d (TAG, "saveObjectToDisk : done.");
		}
		catch (Exception e) {
			Log.e (TAG, "saveObjectToDisk : error while saving to disk. e="+e.toString());
			e.printStackTrace();
		}
		finally {
			mfileAccessSemaphore.release();
			if (os != null) { 
				 try{
					 os.close();
					 // used for test purposes
					 //mContext.deleteFile(file);
				 }
				 catch (Exception e){
				 }
			 }
		}
		
		return success;
	 }
	 
	 /*
	  * reads object from disk
	  * if no object present on disk, returns null
	  */
	@SuppressWarnings("unchecked")
	public synchronized Object readObjectFromDisk(String file) {
		
		 Object o;
		 ObjectInputStream is =null ;

		 Log.d (TAG, "readObjectFromDisk : reading object ...");

		 try {
			 mfileAccessSemaphore.acquire();
			 
			 FileInputStream fis = openFileInput(file);
			 is = new ObjectInputStream(fis);
			 o = is.readObject();	
			 
			 Log.d (TAG, "readObjectFromDisk : done.");
		 }
		 catch (Exception e) {
			 Log.e (TAG, "readObjectFromDisk : error while reading from disk. e="+e.toString());
			 o = null;
			 e.printStackTrace();
		 }	
		 finally {
			 mfileAccessSemaphore.release();
			 if (is != null) { 
				 try{is.close();
				 }
				 catch (Exception e){
				 }
			 }
		 }
		 return o;
	 }
	
	/**
	 * 
	 * @author phid75
	 * thread will create a new MusicCatalog object 
	 *
	 */
	public class MusicEngineBuilder extends Thread {
		
		private boolean refresh;
		private Handler handler;
		
		/**
		 * thread constructor
		 * @param doRefresh : TRUE if we will build a new catalog from scratch, FALSE if we will load saved catalog from disk
		 */
		public MusicEngineBuilder(boolean doRefresh, Handler handler) {
			super ("PlayerApplication - MusicEngineBuilder");	// name the thread
			refresh = doRefresh;
			this.handler = handler;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "MusicEngineBuilder : building catalog  refresh="+refresh);	        	      
			Message message;
			
			mMusicEngine = new MusicEngine(
					getContentResolver(), 
					mMusicDirectories,
					refresh 
					);				

			if (mMusicEngine.getSongsCatalogSize() > 0) {
		        isCatalogReady = true;
		        
		        message = handler.obtainMessage(PlayerService.HANDLER_OK, true);
				handler.sendMessage(message);    	    		
		        
		        // broadcast new catalog to consumers
		        //TODO broadcast to make a Toast "catalog has been rebuilt"
//		        broadcastToActivity(false, false, false);
				
				 if (refresh) {
				        isCatalogUpToDate = true;
				        Log.d(TAG, "MusicEngineBuilder : catalog created. Size is "+ mMusicEngine.getSongsCatalogSize());        		        
				        
				        // we will save it to disk in separate thread
				        Thread t = new CatalogSaver(mMusicEngine);
			            t.start();
			        }
			}			
			else {
				Log.w(TAG, "song catalog is empty");
		        message = handler.obtainMessage(PlayerService.HANDLER_EMPTY_MSG, true);
				handler.sendMessage(message);    	    		
			}
		}

	}
	
	/*
	 * CatalogSaver thread
	 */
	private class CatalogSaver extends Thread {
		
		private MusicEngine musicEngine;
		
		public CatalogSaver(MusicEngine engine) {
			super ("PlayerService - CatalogSaver");	// name the thread
			musicEngine = engine;
		}
		
		@Override
		public void run() {
			Log.d(TAG, "CatalogSaver : saving catalog");	        	      
			
			if (musicEngine.saveCatalogToDisk()) {
				Log.d(TAG, "CatalogSaver : catalog saved to disk");
			}
			else 
				Log.e(TAG, "CatalogSaver : catalog could not be saved to disk");
		}

	}

}

