package net.classicgarage.truerandommusicplayer.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import net.classicgarage.truerandommusicplayer.R;

/**
 * Created by eaton on 9/11/2017.
 */

public class SwipeViewPager extends ViewPager {
    private float preX=0;



    public SwipeViewPager(Context context) {
        super(context);
    }

    public SwipeViewPager(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean res = super.onInterceptTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            preX = ev.getX();
        } else {
            if (Math.abs(ev.getX() - preX) > 4) {
                return true;
            } else {
                preX = ev.getX();
            }
        }
        return res;
    }
}
