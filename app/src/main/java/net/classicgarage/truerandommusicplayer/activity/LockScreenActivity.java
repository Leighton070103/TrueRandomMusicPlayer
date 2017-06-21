package net.classicgarage.truerandommusicplayer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class LockScreenActivity extends AppCompatActivity {

    private static final String TAG = "==LockScreenActivity";

    public WindowManager winManager;
    public RelativeLayout wrapperView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_lock_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(getApplicationContext())){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            }
        }
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams( WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        this.winManager = ((WindowManager)getApplicationContext().getSystemService(WINDOW_SERVICE));
        this.wrapperView = new RelativeLayout(getBaseContext());
        getWindow().setAttributes(localLayoutParams);
        View.inflate(this, R.layout.widget_layout, this.wrapperView);
        this.winManager.addView(this.wrapperView, localLayoutParams);
        Log.d(TAG, "on create");*/
    }

    /*@Override
    protected void onDestroy() {
        this.winManager.removeView(this.wrapperView);
        this.wrapperView.removeAllViews();
        super.onDestroy();
    }*/
}
