package net.classicgarage.truerandommusicplayer.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import java.util.List;

/**
 * Created by wangyunwen on 17/5/24.
 */

public class SwipePagerAdapter extends PagerAdapter{

    private List<View> mViewList;

    public SwipePagerAdapter(List<View> mViewList) {
        this.mViewList = mViewList;
    }

    public void setListViews(List<View> listViews) {// 自行添加数据
        this.mViewList = listViews;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d("pageradapter:", String.valueOf(position)+" is instantiated, list size = " + getCount());
        ((ViewPager) container).addView(mViewList.get( position ),0);
        return mViewList.get(position) ;
    }

    /*@Override
    public void startUpdate(ViewGroup container) {
        super.startUpdate(container);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
    }*/

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView(mViewList.get( position ));
        Log.d("pageradapter:", String.valueOf(position)+" is destroyed");
//        container.removeView(mViewList.get(position % mViewList.size()));
        //container.removeView(mViewList.get(position));
    }
}
