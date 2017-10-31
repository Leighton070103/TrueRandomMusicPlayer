package net.classicgarage.truerandommusicplayer.broadcastreceiver;

/**
 * Created by eaton on 27/10/2017.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import net.classicgarage.truerandommusicplayer.service.BaseService;

public class PhoneStatusReceiver extends BroadcastReceiver {

    private ServiceConnection mServiceCon;
    private BaseService mBaseService;
    private static boolean isCallIncoming = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        mServiceCon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBaseService = (BaseService) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        //dialing a phone call
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            isCallIncoming = false;
            mBaseService.callPause();
            final String phoneNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d("PhoneReceiver", "phoneNum: " + phoneNum);
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
                    mBaseService.callPause();
                    isCallIncoming = true;
                    Log.i("PhoneReceiver", "CALL IN RINGING :" + incomingNumber);
                    break;
                //answered the phone call
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (isCallIncoming) {
                        Log.i("PhoneReceiver", "CALL IN ACCEPT :" + incomingNumber);
                    }
                    break;
                //hang up the phone call
                case TelephonyManager.CALL_STATE_IDLE:
                    if (isCallIncoming) {
                        mBaseService.callPlay();
                        Log.i("PhoneReceiver", "CALL IDLE");
                    }
                    break;
            }
        }
    };
}
