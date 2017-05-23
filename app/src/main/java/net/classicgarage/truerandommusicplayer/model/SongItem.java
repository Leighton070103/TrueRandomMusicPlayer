package net.classicgarage.truerandommusicplayer.model;

import android.content.ContentUris;
import android.net.Uri;
import android.util.Log;

import java.io.Serializable;
import java.util.Comparator;

public class SongItem implements Serializable, Comparable<SongItem> {
    //variable not used
	static final long serialVersionUID = 1;

	private static final String TAG="SongItem";

	private long mIdLog;
	private String mArtistStr;
	private String mTitleStr;
	private String mAlbumStr;
	private long mAlbumIdLog;
	private long mDurationLog;
	private String mPathStr;
	private boolean mFavoriteblo = false;

    public SongItem(){}

    public SongItem(String title, boolean favorite){

        this.mTitleStr = title;
        this.mFavoriteblo = favorite;

    }

    public SongItem(long id, String artist, String title, 
    		long albumId, String album, long duration, String path) {
        this.mIdLog = id;
        this.mArtistStr = artist;
        this.mTitleStr = title;
        this.mAlbumIdLog = albumId;
        this.mAlbumStr = album;
        this.mDurationLog = duration;
        this.mPathStr = path;
        this.mFavoriteblo = false;
    }

    public long getId() {
        return mIdLog;
    }

    public String getArtist() {
        return mArtistStr;
    }

    public String getTitle() {
        return mTitleStr;
    }

    public String getAlbum() {
        return mAlbumStr;
    }

    public long getAlbumId() {
        return mAlbumIdLog;
    }

    public long getDuration() {
        return mDurationLog;
    }
    
    public String getPath() {
        return mPathStr;
    }
    
    public boolean getFavorite() {
        return mFavoriteblo;
    }
    
    public void setFavorite(boolean status) {
    	if(status)
    		Log.d(TAG, "setFavorite song "+getKey()+" to "+status);
    	
    	this.mFavoriteblo = status;
    }
    
    public String toString() {
    	return String.format("%s (%s - %s)", mTitleStr ,mArtistStr, mAlbumStr);
    }
    
    public String getNotificationText(){
    	return toString();
    }

    public String getKey(){
    	return String.format("%s - %s", mArtistStr, mTitleStr);
    }
    
    public String getArtistTitle(){
    	return String.format("%s - %s", mArtistStr, mTitleStr);
    }    
    
    public String toString2() {
    	return String.format("%s - %s - %s)", mArtistStr, mAlbumStr, mTitleStr);
    }
    
    public Uri getURI() {
        return ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mIdLog);
    }

	public int compareTo(SongItem another) {

        return this.toString2().compareTo(another.toString2());
	}


	public static class ArtistAlbumSongsComparator implements Comparator<SongItem>{

		public int compare(SongItem s1, SongItem s2) {
			return s1.toString2().toLowerCase().compareTo(s2.toString2().toLowerCase());
		}
	}

	public static class PathComparator implements Comparator<SongItem>{

		public int compare(SongItem s1, SongItem s2) {
			return s1.getPath().toLowerCase().compareTo(s2.getPath().toLowerCase());
		}
	}

	public void setTitle(String title){
        mTitleStr = title;
    }

    public void setId(long id){
        mIdLog = id;
    }

    public void setPath(String path){
        mPathStr = path;
    }

    public void setAlbum(String album){
        mAlbumStr = album;
    }

    public void setAlbumId(long albumId){
        mAlbumIdLog = albumId;
    }

    public void setArtist(String artist){
        mArtistStr = artist;
    }

    public void resetIsFavorite(){
        if( mFavoriteblo ) mFavoriteblo = false;
        else mFavoriteblo = true;
    }

}


