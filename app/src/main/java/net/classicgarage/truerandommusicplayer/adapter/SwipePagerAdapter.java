package net.classicgarage.truerandommusicplayer.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by wangyunwen on 17/5/24.
 */

public class SwipePagerAdapter extends PagerAdapter{
    private List<View> mViewList;

    public SwipePagerAdapter(List<View> mViewList) {
        this.mViewList = mViewList;
    }

    @Override
    public int getCount() {
        return mViewList.size();
        //return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if(container.getParent()!=null)
            ((ViewGroup)container.getParent()).removeView(mViewList.get(position));
        container.addView(mViewList.get(position));

        //container.addView(mViewList.get(position));
        return mViewList.get(position);
//        container.addView(mViewList.get(position % mViewList.size()));
//        return mViewList.get(position % mViewList.size());
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        container.removeView(mViewList.get(position % mViewList.size()));
        container.removeView(mViewList.get(position));
    }

}
