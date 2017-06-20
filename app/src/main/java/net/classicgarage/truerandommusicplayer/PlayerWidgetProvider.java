package net.classicgarage.truerandommusicplayer;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;


import net.classicgarage.truerandommusicplayer.activity.MusicPlayerActivity;
import net.classicgarage.truerandommusicplayer.service.BaseService;
import net.classicgarage.truerandommusicplayer.service.PlayerService;
import net.classicgarage.truerandommusicplayer.service.PlayerService.PlaybackMode;
import net.classicgarage.truerandommusicplayer.service.PlayerService.PlayerServiceState;
import net.classicgarage.truerandommusicplayer.model.SongItem;
import net.classicgarage.truerandommusicplayer.service.MusicService;

public class PlayerWidgetProvider extends AppWidgetProvider {
	
	private static final String TAG = "PlayerWidgetProvider";
	public static final String WIDGET_ACTION = "Widget action";
	public static final int WIDGET_PLAY_PREVIOUS = 0;
	public static final int WIDGET_OPERATE_CURRENT = 1;
	public static final int WIDGET_PLAY_NEXT = 2;

	/**
	 * Called when the app
	 * @param context
	 * @param appWidgetManager
	 * @param appWidgetIds
	 */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

//        Intent intent = new Intent(context, MusicService.class);
//        context.startService(intent);

        for (int appWidgetId : appWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            //Set intent for playing previous song.
            remoteViews.setOnClickPendingIntent(R.id.widget_pre_btn, getPendingIntentByAction(context,
				WIDGET_PLAY_PREVIOUS));
            //Set intent for play pause button.
            remoteViews.setOnClickPendingIntent(R.id.widget_play_btn, getPendingIntentByAction(context,
				WIDGET_OPERATE_CURRENT));
            // sets intent for play next button
            remoteViews.setOnClickPendingIntent(R.id.widget_next_btn, getPendingIntentByAction(context,
				WIDGET_PLAY_NEXT));
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
      }
	}


	/**
	 * Return the pending intent according to the action.
	 * @param context
	 * @param action
	 * @return
	 */
	private PendingIntent getPendingIntentByAction(Context context, int action){
		Intent intent = new Intent(context, MusicService.class);
		intent.putExtra(WIDGET_ACTION, action);
		return PendingIntent.getService(context, 0, intent, 0);
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context,
										  AppWidgetManager appWidgetManager, int appWidgetId,
										  Bundle newOptions) {
		Log.d(TAG, "onAppWidgetOptionsChanged");


		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
				newOptions);
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
    	/*
		// player state
    	if (intent.getAction().equals(PlayerService.NEW_PLAYER_STATE_INTENT)) {
    		if (intent.hasExtra(PlayerService.INTENT_EXTRA_PLAYER_STATE)) {
		    	mPlayerServiceState = (PlayerServiceState) intent.getSerializableExtra(PlayerService.INTENT_EXTRA_PLAYER_STATE);
				Log.d(TAG, "received broadcast for player state changed to " + mPlayerServiceState.toString());
	   			
				/*
	   			if (mPlayerServiceState == PlayerServiceState.Playing) 
	   				pausedPlaybackByFaceDown = false;	// indicator reset
				*/
	    	//}
    		/*
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
    	*/
    	super.onReceive(context, intent);    	
    }  
    

}