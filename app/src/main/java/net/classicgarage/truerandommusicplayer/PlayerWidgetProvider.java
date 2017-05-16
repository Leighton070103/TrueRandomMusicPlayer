package net.classicgarage.truerandommusicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import net.classicgarage.truerandommusicplayer.activity.MusicPlayerActivity;
import net.classicgarage.truerandommusicplayer.service.PlayerService;
import net.classicgarage.truerandommusicplayer.service.PlayerService.PlaybackMode;
import net.classicgarage.truerandommusicplayer.service.PlayerService.PlayerServiceState;
import net.classicgarage.truerandommusicplayer.model.SongItem;

public class PlayerWidgetProvider extends AppWidgetProvider {
	
	private static final String TAG="PlayerWidgetProvider";
	        
    PlayerServiceState mPlayerServiceState = PlayerServiceState.Inexistant;
    private SongItem songPlaying;
    private String songTitle;
    private PlaybackMode playbackMode = PlaybackMode.RANDOM;

 
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {        	
        	
        	Intent intent;
        	PendingIntent pendingIntent;
        	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);

        	// launch MusicPlayerActivity when click on song playing or main frame                   	
        	intent = new Intent(context, MusicPlayerActivity.class);
        	pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);          	
        	remoteViews.setOnClickPendingIntent(R.id.widgetsongplaying, pendingIntent);  
        	remoteViews.setOnClickPendingIntent(R.id.widgetmainframe, pendingIntent);  
        	
        	// sets intent for PLAY/PAUSE button        	
        	intent = new Intent(context, PlayerService.class);
        	if (mPlayerServiceState == PlayerServiceState.Playing)
        		intent.setAction(PlayerService.ACTION_PAUSE);
            else
                intent.setAction(PlayerService.ACTION_PLAY);
        	
        	pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        	remoteViews.setOnClickPendingIntent(R.id.widgetplaypausebutton, pendingIntent);

        	// sets intent for SKIP button        	
        	intent = new Intent(context, PlayerService.class);     
       		intent.setAction(PlayerService.ACTION_SKIP);
        	pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        	remoteViews.setOnClickPendingIntent(R.id.widgetskipbutton, pendingIntent);
        	
        	// sets intent for REW button        	
        	intent = new Intent(context, PlayerService.class);     
       		intent.setAction(PlayerService.ACTION_REW);
        	pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        	remoteViews.setOnClickPendingIntent(R.id.widgetrewbutton, pendingIntent);
        	
        	// sets intent for STOP button        	
        	intent = new Intent(context, PlayerService.class);     
       		intent.setAction(PlayerService.ACTION_STOP);
        	pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        	remoteViews.setOnClickPendingIntent(R.id.widgetstopbutton, pendingIntent);
        	
        	// set intent for PLAYBACKMODE button
        	intent = new Intent(context, PlayerService.class);     
        	intent.setAction(PlayerService.ACTION_PLAYBACK_MODE);
        	switch (playbackMode) {
        	case RANDOM:        	
           		if (mPlayerServiceState == PlayerServiceState.Playing 
           				|| mPlayerServiceState == PlayerServiceState.Paused) 
        			intent.putExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE, PlayerService.PlaybackMode.RANDOM_FAVORITE);            		
           		break;
        	case RANDOM_FAVORITE:        	
         		if (mPlayerServiceState == PlayerServiceState.Playing 
         				|| mPlayerServiceState == PlayerServiceState.Paused) 
        			intent.putExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE, PlayerService.PlaybackMode.SEQUENTIAL);            		            		
         		break;
        	case SEQUENTIAL:
    			intent.putExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE, PlayerService.PlaybackMode.RANDOM);            		
           		break;
        	}
        	pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        	remoteViews.setOnClickPendingIntent(R.id.widgetshufflebutton, pendingIntent);
        		
        	
        	// update buttons display
    		if (mPlayerServiceState != PlayerServiceState.Playing) {
    			//remoteViews.setImageViewBitmap(R.id.widgetbuttonframe, 
    			//		((BitmapDrawable)context.getResources().getDrawable(R.drawable.frame_widget_dim)).getBitmap());
    			//remoteViews.setImageViewResource(R.id.widgetbuttonframe, R.drawable.frame_widget_dim);
    			remoteViews.setInt(R.id.widgetbuttonframe, "setBackgroundResource", R.mipmap.frame_widget_dim);
    			remoteViews.setImageViewResource(R.id.widgetplaypausebutton, R.drawable.btn_play_nob);
    			remoteViews.setTextColor(R.id.widgetsongplaying, 
    					context.getResources().getColor(R.color.textDim));
    		}
    		else {
    			//remoteViews.setImageViewResource(R.id.widgetbuttonframe, R.drawable.frame_widget); 
    			//remoteViews.setImageViewBitmap(R.id.widgetbuttonframe, 
    			//		((BitmapDrawable)context.getResources().getDrawable(R.drawable.frame_widget)).getBitmap());
    			remoteViews.setInt(R.id.widgetbuttonframe, "setBackgroundResource", R.mipmap.frame_widget);
    			
    			remoteViews.setImageViewResource(R.id.widgetplaypausebutton, R.drawable.btn_pause_nob);
    			remoteViews.setTextColor(R.id.widgetsongplaying, 
    					context.getResources().getColor(R.color.text));  
    		}
    	
    		if (mPlayerServiceState == PlayerServiceState.Inexistant) {
    			remoteViews.setImageViewResource(R.id.widgetskipbutton, R.mipmap.ff_nob_dim);
    			remoteViews.setImageViewResource(R.id.widgetrewbutton, R.mipmap.rew_nob_dim);
    			remoteViews.setImageViewResource(R.id.widgetstopbutton, R.mipmap.stop_nob_dim);
    		}
    		else {
    			remoteViews.setImageViewResource(R.id.widgetskipbutton, R.mipmap.ff_nob);
    			remoteViews.setImageViewResource(R.id.widgetrewbutton, R.mipmap.rew_nob);
    			remoteViews.setImageViewResource(R.id.widgetstopbutton, R.mipmap.stop_nob);
    		}

    		// update song title
    		if (songTitle != null || (mPlayerServiceState != PlayerServiceState.Paused && 
    								   mPlayerServiceState != PlayerServiceState.Playing))
    			remoteViews.setTextViewText(R.id.widgetsongplaying, songTitle);

    		// update Playback mode
    		switch (playbackMode) {
    		case RANDOM:
    			remoteViews.setImageViewResource(R.id.widgetshufflebutton, R.mipmap.shuffle);
    			break;
    		case RANDOM_FAVORITE:
    			remoteViews.setImageViewResource(R.id.widgetshufflebutton, R.mipmap.shuffle_fav);
    			break;
    		case SEQUENTIAL:
    			remoteViews.setImageViewResource(R.id.widgetshufflebutton, R.mipmap.straight);
    			break;
    		}
    		
        	// Tell the AppWidgetManager to perform an update on the current app widget            
        	appWidgetManager.updateAppWidget(appWidgetId, remoteViews);    		
        }
        	
    }
    
    @Override
    public void onEnabled(Context context) {
    	Log.d(TAG, "onEnabled");
    	super.onEnabled(context);
   	}

	@Override
	public void onDisabled(Context context) {
		Log.d(TAG, "onDisabled");
		super.onDisabled(context);
	}

    @Override	
    public void onReceive (Context context, Intent intent) {   	 
    	Log.d(TAG, "onReceive " + intent.getAction());
    	
		// player state
    	if (intent.getAction().equals(PlayerService.NEW_PLAYER_STATE_INTENT)) {
    		if (intent.hasExtra(PlayerService.INTENT_EXTRA_PLAYER_STATE)) {
		    	mPlayerServiceState = (PlayerServiceState) intent.getSerializableExtra(PlayerService.INTENT_EXTRA_PLAYER_STATE);
				Log.d(TAG, "received broadcast for player state changed to " + mPlayerServiceState.toString());
	   			
				/*
	   			if (mPlayerServiceState == PlayerServiceState.Playing) 
	   				pausedPlaybackByFaceDown = false;	// indicator reset
				*/
	    	}    		
    		
    		// song title
    		if (intent.hasExtra(PlayerService.INTENT_EXTRA_SONG_PLAYING)) {
    			songPlaying = (SongItem) intent.getSerializableExtra(PlayerService.INTENT_EXTRA_SONG_PLAYING);    			    	
    			
    			if (songPlaying != null){
    				songTitle = songPlaying.toString();
    			}
    			else
    				songTitle = null;
    			
    			Log.d(TAG, "received broadcast for songTitle changed to " + songTitle );
    		}
    		
    		// Play Mode
    		if (intent.hasExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE)) {
    			playbackMode = (PlaybackMode) intent.getSerializableExtra(PlayerService.INTENT_EXTRA_PLAYBACK_MODE);
    			Log.d(TAG, "received broadcast for play mode changed");
    		}
    		
        	// force update
        	AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        	this.onUpdate(context, widgetManager, 
        			widgetManager.getAppWidgetIds(new ComponentName(context, PlayerWidgetProvider.class)));
    	}
    	else if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
    		// since we don't update periodically this widget, will occur only when widget is created
    		// we need to ask player service to broadcast the status and song played if any
    		// context.startService(new Intent(PlayerService.ACTION_BROADCAST));
			context.startService(new Intent(context, PlayerService.class));
    	}
    		        	
    	super.onReceive(context, intent);    	
    }  
    

}