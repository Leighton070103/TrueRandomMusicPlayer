package net.classicgarage.truerandommusicplayer.broadcastreceiver;

/**
 * Created by eaton on 27/10/2017.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import net.classicgarage.truerandommusicplayer.service.MusicService;


public class PhoneStatusReceiver extends BroadcastReceiver {

    private static boolean isCallIncoming = false;
    private Intent mPhoneCallIntent;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        mPhoneCallIntent = new Intent(context, MusicService.class);
        //dialing a phone call
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            isCallIncoming = false;
            mPhoneCallIntent.putExtra(MusicService.INTENT_ACTION,MusicService.ACTION_PAUSE);
            final String phoneNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d("PhoneReceiver", "phoneNum: " + phoneNum);
            this.context.startService(mPhoneCallIntent);
        } else {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    final PhoneStateListener listener=new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch(state){
                //Ringing
                case TelephonyManager.CALL_STATE_RINGING:
                    mPhoneCallIntent.putExtra(MusicService.INTENT_ACTION,MusicService.ACTION_PAUSE);
                    isCallIncoming = true;
                    Log.i("PhoneReceiver", "CALL IN RINGING :" + incomingNumber);
                    context.startService(mPhoneCallIntent);
                    break;
                //answering the phone call
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (isCallIncoming) {
                        mPhoneCallIntent.putExtra(MusicService.INTENT_ACTION,MusicService.ACTION_PAUSE);
                        Log.i("PhoneReceiver", "CALL IN ACCEPT :" + incomingNumber);
                        context.startService(mPhoneCallIntent);
                    }
                    break;
                //hang up the phone call
                case TelephonyManager.CALL_STATE_IDLE:
                    if (isCallIncoming) {
                        mPhoneCallIntent.putExtra(MusicService.INTENT_ACTION,MusicService.ACTION_PAUSE);
                        Log.i("PhoneReceiver", "CALL IDLE");
                        context.startService(mPhoneCallIntent);
                    }
                    break;
            }

        }
    };
}
