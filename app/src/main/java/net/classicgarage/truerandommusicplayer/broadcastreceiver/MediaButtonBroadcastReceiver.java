/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.classicgarage.truerandommusicplayer.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import net.classicgarage.truerandommusicplayer.PlayerWidgetProvider;
import net.classicgarage.truerandommusicplayer.service.MusicService;
import net.classicgarage.truerandommusicplayer.service.PlayerService;

/**
 * Receives broadcasted intents. In particular, we are interested in the
 * android.media.AUDIO_BECOMING_NOISY, which is
 * broadcast, for example, when the user disconnects the headphones
 * we have register to this broadcast in the manifest file
 */
public class MediaButtonBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG="MediaButtonBroadcastReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {

        String intentAction = intent.getAction();
        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        Log.i(TAG, "Action ---->" + intentAction + "  KeyEvent----->"+ keyEvent.toString());

        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            // get key code.
            Intent buttonIntent = new Intent(context, MusicService.class);
            int keyCode = keyEvent.getKeyCode();
            switch ( keyCode ){
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    intent.putExtra( PlayerWidgetProvider.WIDGET_ACTION, PlayerWidgetProvider
                            .WIDGET_PLAY_NEXT);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    intent.putExtra( PlayerWidgetProvider.WIDGET_ACTION, PlayerWidgetProvider
                            .WIDGET_OPERATE_CURRENT);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    intent.putExtra( PlayerWidgetProvider.WIDGET_ACTION, PlayerWidgetProvider
                            .WIDGET_PLAY_PREVIOUS);
                    break;
            }
            context.startService(buttonIntent);
        }
    }    
}
