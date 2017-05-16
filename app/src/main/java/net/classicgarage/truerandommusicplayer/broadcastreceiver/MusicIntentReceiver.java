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
import android.widget.Toast;

import net.classicgarage.truerandommusicplayer.service.PlayerService;

/**
 * Receives broadcasted intents. In particular, we are interested in the
 * android.media.AUDIO_BECOMING_NOISY, which is
 * broadcast, for example, when the user disconnects the headphones
 * we have register to this broadcast in the manifest file
 */
public class MusicIntentReceiver extends BroadcastReceiver {

	private static final String TAG="MusicIntentReceiver";	
    
    @Override
    public void onReceive(Context context, Intent intent) {

        // AudioManager.ACTION_AUDIO_BECOMING_NOISY:
        // Broadcast intent, a hint for applications that audio is about to become 'noisy'
        // due to a change in audio outputs.
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Toast.makeText(context, "Headphones disconnected", Toast.LENGTH_SHORT).show();

            // send an intent to PlayerService to tell it to pause the audio
            Log.d(TAG, "sending PAUSE intent");
            context.startService(new Intent(PlayerService.ACTION_PAUSE));
        }
    }    
}
