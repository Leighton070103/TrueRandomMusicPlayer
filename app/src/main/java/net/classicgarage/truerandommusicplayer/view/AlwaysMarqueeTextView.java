package net.classicgarage.truerandommusicplayer.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * This class is to provide a textview widget that would automatically hide part of the text if it
 * is too long.
 * Created by Tong on 2017/6/21.
 */

public class AlwaysMarqueeTextView extends android.support.v7.widget.AppCompatTextView {
    public AlwaysMarqueeTextView(Context context) {
        super(context);
    }
    public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
