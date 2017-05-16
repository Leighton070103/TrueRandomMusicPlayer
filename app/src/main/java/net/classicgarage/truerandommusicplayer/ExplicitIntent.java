package net.classicgarage.truerandommusicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.Nullable;

import java.util.List;


public class ExplicitIntent extends Intent{

    public ExplicitIntent(Context context, Intent intent){
        createExplicitFromImplicitIntent(context, intent);
    }

    @Nullable
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent){
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        if(resolveInfo == null || resolveInfo.size() != 1){
            return null;
        }
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName,className);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}
