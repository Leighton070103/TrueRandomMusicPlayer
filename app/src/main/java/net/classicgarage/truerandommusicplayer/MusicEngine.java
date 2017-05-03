package net.classicgarage.truerandommusicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Songs and Catalog management
 * @author phid75
 *MediaMetadataCompat we dont need to use the songIETM
 */
public class MusicEngine {

	private ContentResolver mContentResolver;	// will be queried to build catalog
	private List<String> mMusicFileLocations;	// directories on SD card where songs are located
	private PlayerApplication mApplication;
	
	private ArrayList<SongItem> mSongsCatalog;	//song of sequence
	private displayTypes mCatalogSortOrder;		// already order the song of sequence
	private HashSet<String> mFavoriteSongs;		// set of favorite songs identified by ARTIST+SONG key
		
	static private final String CATALOG_FILE = "catalog.dat";		// catalog backup file
	static private final String FAVORITES_FILE = "favorites.dat";		// favorite songs list
	
	public enum displayTypes {
		SONGDISPLAY,
		PATHDISPLAY
	};
	
	private static final String TAG="MusicEngine";
	
	Random randomGenerator;
	
	/**
	 * creates a song catalog based on this base directory
	 * @param cr : content resolver
	 * @param directories : base directories from where to build catalog 
	 * @param refresh = TRUE if we will build a new catalog from scratch, FALSE if we will load saved catalog from disk
	 */
	public MusicEngine(
			ContentResolver cr, 
			List<String> directories, 
			boolean refresh) {
		
		Log.i(TAG, "MusicEngine. refresh="+refresh);
		
		mContentResolver = cr;		
		mMusicFileLocations = directories;
		mApplication = PlayerApplication.getInstance();	// reference to Application singleton
		
		mFavoriteSongs = readFavoritesFromDisk();		// retrieve list of favorite songs, if any
		if (mFavoriteSongs == null) {
			Log.d(TAG, "no Favorites were found on disk");
			
			mFavoriteSongs = new HashSet<String>();			
		}		
		else
			Log.d(TAG, "Favorites loaded from disk. Found "+mFavoriteSongs.size()+" items.");

        randomGenerator = new Random();   
        
        if (refresh) {	  	// we build from scratch
			mSongsCatalog = new ArrayList<SongItem>();

        	createSongsCatalog(mSongsCatalog, displayTypes.SONGDISPLAY);
        }
		else {	// load it from disk, if a copy is available
			Log.i(TAG, "attempting to load songs catalog from disk ...");
			mSongsCatalog = readCatalogFromDisk();
		}
	}
	
	public ArrayList<SongItem> getSongsCatalog() {
		return mSongsCatalog;
	}
	
	public void setSongsCatalog(ArrayList<SongItem> catalog) {
		mSongsCatalog = catalog;
	}
	
	public int getSongsCatalogSize() {
		return (mSongsCatalog != null ? mSongsCatalog.size() : 0);
	}
	
	public ListIterator<SongItem> getSongsCatalogIterator(SongItem song) {
		int currentIndex = (song != null  ? mSongsCatalog.indexOf(song) : 0); 
		if (currentIndex == getSongsCatalogSize()-1) 
			currentIndex = -1;

		return mSongsCatalog.listIterator(currentIndex+1);
	}
	
	public displayTypes getCatalogSortOrder() {
		Log.d(TAG, "getCatalogSortOrder : sort order is "+mCatalogSortOrder);

		return mCatalogSortOrder;
	}

	public void setCatalogSortOrder(displayTypes catalogSortOrder) {
		Log.d(TAG, "catalog sort order changed to "+catalogSortOrder);
		
		this.mCatalogSortOrder = catalogSortOrder;
	}

	
	/**
	 * creates the song catalog
	 * we will create an ordered song list. This is the sort order that will be used when not in random mode
	 * @param reference to a SongCatalog to populate
	 *                  
	 */
	public void createSongsCatalog(ArrayList<SongItem> catalog, displayTypes displayType){

		Log.i(TAG, "creating songs catalog from scratch ...");

		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {android.provider.MediaStore.Audio.Media._ID,
				android.provider.MediaStore.Audio.Media.TITLE,
				android.provider.MediaStore.Audio.Media.ARTIST,
				android.provider.MediaStore.Audio.Media.ALBUM_ID,
				android.provider.MediaStore.Audio.Media.ALBUM,
				android.provider.MediaStore.MediaColumns.DATA
		};

		String[] whereArgs = new String[] {mMusicFileLocations.get(0)+File.separator+"%"};
		//Log.d(TAG, "whereArgs="+whereArgs[0]);
		//TODO update for multiple directories

		String orderBy=null;
		if (displayType.equals(displayTypes.SONGDISPLAY)) {
			orderBy = "LOWER("+android.provider.MediaStore.Audio.Media.ARTIST+"), "
				+"LOWER ("+android.provider.MediaStore.Audio.Media.ALBUM+"), "
				+"LOWER ("+android.provider.MediaStore.Audio.Media.TRACK+")";	
		}
		else if (displayType.equals(displayTypes.PATHDISPLAY)) {
			orderBy = "LOWER("+android.provider.MediaStore.Audio.Media.DATA+")";
		}
				

		Cursor cursor = mContentResolver.query(
				uri, 
				projection, 
				android.provider.MediaStore.MediaColumns.DATA+"  like ?", 
				whereArgs,
				orderBy);

		if (cursor == null) {    
			// query failed, handle error.
			Log.e(TAG, "createSongsCatalog : query cursor is NULL");
		}
		else if (!cursor.moveToFirst()) {    
			// no media on the device
			Log.i(TAG, "createSongsCatalog : no songs found on device");
		} 
		else {    
			int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);   
			int artistColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			int albumIdColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID);	    		
			int albumColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM);  
			int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);   
			int pathColumn = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA);
			do {       
				SongItem thisSong = new SongItem(
						cursor.getLong(idColumn),
						cursor.getString(artistColumn),
						cursor.getString(titleColumn),
						cursor.getLong(albumIdColumn),
						cursor.getString(albumColumn),
						0,
						cursor.getString(pathColumn)
						);

				//Log.i(TAG, cursor.getString(pathColumn));

				// check if song is a favorite
				if (mFavoriteSongs != null && mFavoriteSongs.contains(thisSong.getKey())) {
					//Log.i(TAG, "createSOngsCatalog : song "+thisSong.getTitle()+" is a favorite");
					thisSong.setFavorite(true);
				}

				catalog.add(thisSong);

			} while (cursor.moveToNext());
		}
		
		// update current display type
		mCatalogSortOrder = displayType;

	}
	
/**
 * delete a song from catalog
 * @param song
 * @return number of items deleted
 */
	public int deleteSong(SongItem song){

		Log.i(TAG, "deleting song "+song.toString());


		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String whereClause = android.provider.MediaStore.Audio.Media._ID + "=?";	    	
		String[] whereArgs = new String[] {Long.toString(song.getId())};
		Log.d(TAG, "whereArgs="+whereArgs[0]);

		int rowsDeleted = mContentResolver.delete(uri, whereClause, whereArgs);

		if (rowsDeleted == 1) {
    		// delete song from catalog
			synchronized (mSongsCatalog) {
				if (!mSongsCatalog.remove(song)) {
					Log.e(TAG, "deleteSong : could not remove song from arrayList");
				}
				else {
					// remove from favorites
					setFavorite(song.getKey(), false);
					saveFavoritesToDisk();
				}
			}
		}
		return rowsDeleted;
	}

	/**
	 * gets a random song from catalog
	 * it also not a true random song>?
	 * @return SongItem
	 */
	 public SongItem getRandomSong() {
		 SongItem thisSong;
		 
		synchronized (mSongsCatalog) {
             
			thisSong = mSongsCatalog.get(randomGenerator.nextInt(getSongsCatalogSize()));
		}
		Log.d(TAG, "getRandomSong :"+thisSong.toString());	
		return thisSong;
	 }

	/**
	 * gets a song from catalog at a specific position
	 * @param id : song position
	 * @return SongItem
	 */
	 public SongItem getSongAt(Long id) {

		 SongItem thisSong;

		 synchronized (mSongsCatalog) {
			 thisSong = mSongsCatalog.get(0);

			 for (SongItem s: mSongsCatalog) {
				 if (s.getId()== id) {
					 thisSong = s;
					 break;
				 }
			 }
		 }

		 Log.d(TAG, "getSongAt id="+id+" song=" +thisSong);	
		 return thisSong;
	 }
	 
	 /**
	  * return song with this favorite key is in catalog
	  * @param favoriteKey
	  * @return song
	  */
	 public SongItem getSongAt(String favoriteKey) {
		 SongItem song = null;

		 Iterator<SongItem> it2 = mSongsCatalog.iterator();

		 while (it2.hasNext()) {
			 song = it2.next();
			 if (song.getKey().equals(favoriteKey)) {
				 break;
			 }
		 }
		 return song;
	 }

	/**
	 * gets a random favorite song from catalog
	 * @return SongItem
	 * how to get the random: get a random index from the account of favoriteSong but i dont know why code need to iterator the list???
	 * it is not a true romdam
	 * should we need a flag which means already run....in the songItem
	 */
	 public SongItem getRandomFavoriteSong() {
		 SongItem thisSong = null;
		 String key = null;
		 int retry = 0;

		 synchronized (mSongsCatalog) {

			 Random r = new Random();
			 if (mFavoriteSongs.size() >0) {
				 // retry if favorite song picked up is not in catalog
				 while (thisSong == null && retry<3) {
					 Iterator<String> it = mFavoriteSongs.iterator();
					 // iterate a random number of times 
					 int n = r.nextInt(mFavoriteSongs.size()-1)+1;
					 Log.d(TAG, "getRandomFavoriteSong: random pick r="+n);

					 for (int i=0; i<=n ;i++) {
						 if (it.hasNext()) key=it.next();
					 }

					 thisSong = getSongAt(key);

					 if (thisSong != null) 
						 Log.d(TAG, "getRandomFavoriteSong :"+thisSong.toString());		
					 else {
						 Log.e(TAG, "getRandomFavoriteSong : song "+key+" was NOT foung in catalog. Will pick another one");
						 retry++;
						 /*what he meaning of the retry???*/
					 }
				 }			
			 }
			 else {
				 Log.e(TAG, "getRandomFavoriteSong : unable to get new song. favorite songs list is empty"); 
			 }
		 }
	        		        
		return thisSong;
	 }
			 
	 
	/**
	 * gets next song from catalog
	 * @param catalogIterator : iterator on songs catalog
	 * @return SongItem
	 */
	 public SongItem getNextSong(ListIterator<SongItem> catalogIterator) {

		 SongItem thisSong = null;

		 synchronized (mSongsCatalog) {

			 try {
				 Log.d(TAG, "getNextSong : next index = "+catalogIterator.nextIndex());

				 thisSong = catalogIterator.next() ; 
				 Log.d(TAG, "getNextSong :"+thisSong.toString());
			 }
			 catch (NoSuchElementException e) {  // ne devrait jamais arriver
				 Log.e(TAG, "getNextSong : got a NoSuchElementExecpetion !!");
				 thisSong = mSongsCatalog.get(0) ;
			 }
			 catch (Exception e) {  // ne devrait jamais arriver
				 Log.e(TAG, "getNextSong : got an exception "+e.getMessage());
				 e.printStackTrace();
				 thisSong = mSongsCatalog.get(0) ;
			 }
		 }
		 return thisSong;
	 }
	 
/**
 * get album art
 * @param albumId
 * @param height
 * @param width
 * @return Bitmap
 */
	 public Bitmap getAlbumArt(long albumId, int height, int width) {
		 Bitmap bm = null;     
		 
		 //TODO use a cache, either the mediaStore cache or a private cache ou les 2
		 
		 try
		 {
			 final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");              
			 Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);              
			 ParcelFileDescriptor pfd = mContentResolver.openFileDescriptor(uri, "r");              
			 if (pfd != null)              
			 {                 
				 FileDescriptor fd = pfd.getFileDescriptor();
				 bm = BitmapFactory.decodeFileDescriptor(fd);   //get the picture and do some things in this piectur
				 }     
			 } 
		 catch (FileNotFoundException e) {
			 Log.i(TAG, "could find album art from URI ");
		 }
		catch (Exception e) { 
			 Log.e(TAG, "could not open album art from URI ");
			 e.printStackTrace();			 
		 }     
		 return (bm == null ? null : Bitmap.createScaledBitmap(bm, width, height, false)); 
	 }

/**
 * updates favorite songs list
 * @param key : song key
 * @param status : true if we want to set as fovorite, false if we want to remove it from favorites
 */
	 public void setFavorite (String key, boolean status) {
		 Log.d(TAG, "updateFavoriteSongsList for key="+key+" and status="+status);
		 
		 // list has the song but it's no longer a favorite
		 if (mFavoriteSongs.contains(key) && !status)
			 mFavoriteSongs.remove(key);
		 
		 // the song is a favorite and list doesn't have it yet
		 if (!mFavoriteSongs.contains(key) && status)
			 mFavoriteSongs.add(key);
	 }
	 
	 /**
	  * clcean favorites from songs no longer in catalog
	  */
	 public void cleanFavorites () {
		Log.i(TAG, "cleanfavorites START");
		
		String key;
		
		Iterator<String> it = mFavoriteSongs.iterator();

		while (it.hasNext()) {
			key=it.next();
			
			if (getSongAt(key) == null) {
				Log.d(TAG, "cleanFavorites: "+key+" was not in catalog. Deleting from favorites");
				
				synchronized (mFavoriteSongs) {
					mFavoriteSongs.remove(key);
				}
			}
		 }
	 }
		 


	 /**
	  * saves catalog to disk
	  * @return true or false
	  */
	 public synchronized boolean saveCatalogToDisk(){
			return mApplication.saveObjectToDisk(CATALOG_FILE, mSongsCatalog);
	 }
	 
	 /**
	  * saves favorites to disk
	  * @return true or false
	  */
	 public synchronized boolean saveFavoritesToDisk(){
			return mApplication.saveObjectToDisk(FAVORITES_FILE, mFavoriteSongs);
	 }
	 

	 
	 /**
	  * reads catalog from disk
	  * if no catalog present on disk, returns null
	  * @return ArrayList<SongItem>  
	  */
	public synchronized ArrayList<SongItem> readCatalogFromDisk() {
		
		 return (ArrayList<SongItem>) mApplication.readObjectFromDisk(CATALOG_FILE);
	 }
	
	 /**
	  * reads favorite songs list from disk
	  * if no favorites present on disk, returns null
	  * @return HashSet<String>
	  */
	public synchronized HashSet<String> readFavoritesFromDisk() {
		
		 return (HashSet<String>) mApplication.readObjectFromDisk(FAVORITES_FILE);
	 }
	 
	}
