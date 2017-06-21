package net.classicgarage.truerandommusicplayer.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Comparator;

public class SongItem implements Serializable, Comparable<SongItem> {

    public static final String SONG_ID = "songId";
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
    private int mPlayedTime = 0;

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
    private static final Uri albumIdUri = Uri.parse("content://media/external/audio/albumart");

    public static Bitmap getArtworkFromFile(Context context, long songId, long albumId) throws IllegalAccessException {
        Bitmap bm = null;
        if(albumId < 0 && songId <0) {
            throw new IllegalAccessException("Must specify an album or a song id");
        }
        try{
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if(albumId < 0){
                Uri uri = Uri.parse("content://media/external/audio/media"+ songId +"a/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri,"r");
                if(pfd != null){
                    fd = pfd.getFileDescriptor();
                }
            }
            else {
                Uri uri = ContentUris.withAppendedId(albumIdUri,albumId);
                ParcelFileDescriptor pdf = context.getContentResolver().openFileDescriptor(uri,"r");
                if(pdf != null){
                    fd = pdf.getFileDescriptor();
                }
            }
            options.inSampleSize =1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd,null,options);
            options.inSampleSize = 100;
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bm = BitmapFactory.decodeFileDescriptor(fd,null,options);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return  bm;
    }
    public static Bitmap getArtwork(Context context, long song_id, long album_id,boolean small)
    {
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumIdUri,album_id);
        if(uri != null)
        {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in,null,options);
                if(small)
                {
                    options.inSampleSize = computeSampleSize(options,300);
                }
                else
                {
                    options.inSampleSize = computeSampleSize(options,2000);
                }
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in,null,options);
            } catch (FileNotFoundException e) {
                Bitmap bm = null;
                try {
                    bm = getArtworkFromFile(context,song_id,album_id);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }finally {
                try {
                    if(in != null){
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static int computeSampleSize(BitmapFactory.Options options, int targe) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w/ targe;
        int candidateH = h/ targe;
        int cadidate = Math.max(candidateH,candidateW);
        if(cadidate == 0) return 1;
        if(cadidate > 1){
            if((w > targe) && (w/cadidate)<targe)
            {
                cadidate -=1;
            }
        }
        if(cadidate > 1){
            if((h > targe) && (h/cadidate)<targe)
            {
                cadidate -=1;
            }
        }
        return cadidate;
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
    	return getTitle();
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

    public int getmPlayedTime() {
        return mPlayedTime;
    }

    public void setmPlayedTime(int mPlayedTime) {
        this.mPlayedTime = mPlayedTime;
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


