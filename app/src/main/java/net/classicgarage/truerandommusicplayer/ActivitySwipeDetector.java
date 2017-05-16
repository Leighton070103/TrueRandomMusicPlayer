package net.classicgarage.truerandommusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import net.classicgarage.truerandommusicplayer.service.PlayerService;

class ActivitySwipeDetector implements View.OnTouchListener {

static final String TAG = "ActivitySwipeDetector";
private Activity mActivityAct;
private static final int SWIPE_MIN_DISTANCE = 100;
private int mSwipeMinDistance = 100;

private float mDownXFlt, mDownYFlt, mUpXFlt, mUpYFlt;

ActivitySwipeDetector(Activity mActivityAct){
    this.mActivityAct = mActivityAct;
    
    DisplayMetrics dm = mActivityAct.getResources().getDisplayMetrics();
    mSwipeMinDistance = (int)(SWIPE_MIN_DISTANCE * dm.densityDpi / 160.0f);
}

private void onRightToLeftSwipe(){
    Log.i(TAG, "RightToLeftSwipe!");
    mActivityAct.startService(new Intent(PlayerService.ACTION_SKIP));
}

private void onLeftToRightSwipe(){
    Log.i(TAG, "LeftToRightSwipe!");
    mActivityAct.startService(new Intent(PlayerService.ACTION_REW));
}

private void onTopToBottomSwipe(){
    Log.i(TAG, "onTopToBottomSwipe!");
    // do nothing
}

private void onBottomToTopSwipe(){
    Log.i(TAG, "onBottomToTopSwipe!");
    // do nothing
}

public boolean onTouch(View v, MotionEvent event) {
    switch(event.getAction()){
        case MotionEvent.ACTION_DOWN: {
            mDownXFlt = event.getX();
            mDownYFlt = event.getY();
            return true;
        }
        case MotionEvent.ACTION_UP: {
            mUpXFlt = event.getX();
            mUpYFlt = event.getY();

            float deltaX = mDownXFlt - mUpXFlt;
            float deltaY = mDownYFlt - mUpYFlt;

            // swipe horizontal?
            if(Math.abs(deltaX) > mSwipeMinDistance){
                // left or right
                if(deltaX < 0) { this.onLeftToRightSwipe(); return true; }
                if(deltaX > 0) { this.onRightToLeftSwipe(); return true; }
            }
            else {
                    Log.i(TAG, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + mSwipeMinDistance);
                    return false; // We don't consume the event
            }

            // swipe vertical?
            if(Math.abs(deltaY) > mSwipeMinDistance){
                // top or down
                if(deltaY < 0) { this.onTopToBottomSwipe(); return true; }
                if(deltaY > 0) { this.onBottomToTopSwipe(); return true; }
            }
            else {
                    Log.i(TAG, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + mSwipeMinDistance);
                    return false; // We don't consume the event
            }

            return true;
        }
    }
    return false;
}

}
