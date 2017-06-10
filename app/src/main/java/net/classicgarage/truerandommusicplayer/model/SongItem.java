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

    private int mSequenceLog;
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
        int index = 0;
        index = mTitleStr.indexOf("-");
        if(index > 0 && index < mTitleStr.length()-1)
        {
            return mTitleStr.substring(0,index);
        }
        if(mTitleStr.length()>30)
            return mTitleStr.substring(0,30);
        else
            return mTitleStr;
    }

    public String getAlbum() {
        int index = 0;
        index = mAlbumStr.indexOf("(");
        if(index > 0 && index < 30) return mAlbumStr.substring(0,index);
        if(mAlbumStr.length() > 30) return mAlbumStr.substring(0,30);
        return mAlbumStr;
    }

    public long getAlbumId() {
        return mAlbumIdLog;
    }
//  instead of getDuration use the getSongtime()
    private long getDuration() {
        return mDurationLog;
    }

    public String getSongTime(){
        long time = getDuration();
        String formatedTime = formateTime(time);
        return formatedTime;
    }
    public static String formateTime(long time){
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        }else{
            min = time / (1000 * 60) + "";
        }
        if(sec.length() == 4){
            sec = "0" + ( time % (1000 * 60)) + "";
        }else if(sec.length() == 3){
            sec = "00" + ( time % (1000 * 60)) + "";
        }else if(sec.length() == 2){
            sec = "000" + ( time % (1000 * 60)) + "";
        }else if(sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0,2);
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

    public int getSequenceLog() {
        return mSequenceLog;
    }

    public void setSequenceLog(int mSequenceLog) {
        this.mSequenceLog = mSequenceLog;
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

    public void setDuration(long duration) {mDurationLog = duration;}


    public void resetIsFavorite(){
        if( mFavoriteblo ) mFavoriteblo = false;
        else mFavoriteblo = true;
    }

}


