package net.classicgarage.truerandommusicplayer.broadcastreceiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

import net.classicgarage.truerandommusicplayer.activity.LockScreenActivity;

public class LockScreenBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        if(intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
//            KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(context.KEYGUARD_SERVICE);
//            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
//            keyguardLock.disableKeyguard();
//            Intent localIntent = new Intent(context, LockScreenActivity.class);
//            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            localIntent.addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
//            context.startActivity(localIntent);
//        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

            Intent intent11 = new Intent(context,LockScreenActivity.class);
            intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent11);

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Intent intent11 = new Intent(context,LockScreenActivity.class);
            intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        }
        else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {

            Intent intent11 = new Intent(context, LockScreenActivity.class);
            intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent11);

        }
    }
}
