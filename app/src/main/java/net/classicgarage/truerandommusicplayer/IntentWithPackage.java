package net.classicgarage.truerandommusicplayer;

import android.content.Context;
import android.content.Intent;

/**
 * Created by tomat on 2017-05-16.
 */

public class IntentWithPackage extends Intent {

    public IntentWithPackage(Context context, String service){
        setPackage(context, service);
    }

    public Intent setPackage(Context context,String service){
        Intent intent = new Intent(service);
        intent.setPackage(context.getPackageName());
        return intent;
    }
}
