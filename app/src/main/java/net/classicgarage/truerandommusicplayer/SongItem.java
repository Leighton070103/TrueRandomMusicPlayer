package net.classicgarage.truerandommusicplayer;

import android.content.ContentUris;
import android.net.Uri;
import android.util.Log;

import java.io.Serializable;
import java.util.Comparator;

public class SongItem implements Serializable, Comparable<SongItem> {

	static final long serialVersionUID = 1;

	private static final String TAG="SongItem"; 
	
	private long id;
	private String artist;
	private String title;
	private String album;
	private long albumId;
	private long duration;
	private String path;
	private boolean favorite;

    public SongItem(long id, String artist, String title, 
    		long albumId, String album, long duration, String path) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.albumId = albumId;
        this.album = album;
        this.duration = duration;
        this.path = path;
        this.favorite = false;
    }

    public long getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public long getDuration() {
        return duration;
    }
    
    public String getPath() {
        return path;
    }
    
    public boolean getFavorite() {
        return favorite;
    }
    
    public void setFavorite(boolean status) {
    	if(status)
    		Log.d(TAG, "setFavorite song "+getKey()+" to "+status);
    	
    	this.favorite = status;
    }
    
    public String toString() {
    	return String.format("%s (%s - %s)", title ,artist, album);
    }
    
    public String getNotificationText(){
    	return toString();
    }

    public String getKey(){
    	return String.format("%s - %s", artist, title);
    }
    
    public String getArtistTitle(){
    	return String.format("%s - %s", artist, title);
    }    
    
    public String toString2() {
    	return String.format("%s - %s - %s)", artist, album, title);
    }
    
    public Uri getURI() {
        return ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
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
	   
}


