package net.classicgarage.truerandommusicplayer.util;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Tong on 2017/5/16.
 */

public class IntentHelper {
    public static Intent getExplicitIntentForService(Context context, String service){
        Intent intent = new Intent(service);
        intent.setPackage(context.getPackageName());
        return intent;
    }
}
