package net.classicgarage.truerandommusicplayer.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.classicgarage.truerandommusicplayer.activity.LockScreenActivity;

/**
 * This broadcast receiver is to receive broadcast for the lock screen message.
 */
public class LockScreenBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if(action.equals(Intent.ACTION_SCREEN_OFF) ) {
            Intent i = new Intent(context, LockScreenActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

    }
}
