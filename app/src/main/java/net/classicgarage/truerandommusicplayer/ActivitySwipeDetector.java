package net.classicgarage.truerandommusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ActivitySwipeDetector implements View.OnTouchListener {

static final String TAG = "ActivitySwipeDetector";
private Activity activity;
static final int SWIPE_MIN_DISTANCE = 100;
private int REL_SWIPE_MIN_DISTANCE = 100;

private float downX, downY, upX, upY;

public ActivitySwipeDetector(Activity activity){
    this.activity = activity;
    
    DisplayMetrics dm = activity.getResources().getDisplayMetrics();
    REL_SWIPE_MIN_DISTANCE = (int)(SWIPE_MIN_DISTANCE * dm.densityDpi / 160.0f);
}

public void onRightToLeftSwipe(){
    Log.i(TAG, "RightToLeftSwipe!");
    activity.startService(new Intent(PlayerService.ACTION_SKIP));
}

public void onLeftToRightSwipe(){
    Log.i(TAG, "LeftToRightSwipe!");
    activity.startService(new Intent(PlayerService.ACTION_REW));
}

public void onTopToBottomSwipe(){
    Log.i(TAG, "onTopToBottomSwipe!");
    // do nothing
}

public void onBottomToTopSwipe(){
    Log.i(TAG, "onBottomToTopSwipe!");
    // do nothing
}

public boolean onTouch(View v, MotionEvent event) {
    switch(event.getAction()){
        case MotionEvent.ACTION_DOWN: {
            downX = event.getX();
            downY = event.getY();
            return true;
        }
        case MotionEvent.ACTION_UP: {
            upX = event.getX();
            upY = event.getY();

            float deltaX = downX - upX;
            float deltaY = downY - upY;

            // swipe horizontal?
            if(Math.abs(deltaX) > REL_SWIPE_MIN_DISTANCE){
                // left or right
                if(deltaX < 0) { this.onLeftToRightSwipe(); return true; }
                if(deltaX > 0) { this.onRightToLeftSwipe(); return true; }
            }
            else {
                    Log.i(TAG, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + REL_SWIPE_MIN_DISTANCE);
                    return false; // We don't consume the event
            }

            // swipe vertical?
            if(Math.abs(deltaY) > REL_SWIPE_MIN_DISTANCE){
                // top or down
                if(deltaY < 0) { this.onTopToBottomSwipe(); return true; }
                if(deltaY > 0) { this.onBottomToTopSwipe(); return true; }
            }
            else {
                    Log.i(TAG, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + REL_SWIPE_MIN_DISTANCE);
                    return false; // We don't consume the event
            }

            return true;
        }
    }
    return false;
}

}
