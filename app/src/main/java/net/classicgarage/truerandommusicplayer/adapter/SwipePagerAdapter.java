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
    private int size;// 页数

    public SwipePagerAdapter(List<View> mViewList) {
        this.mViewList = mViewList;
        size = mViewList == null ? 0 : mViewList.size();
    }

    public void setListViews(List<View> listViews) {// 自行添加数据
        this.mViewList = listViews;
        size = listViews == null ? 0 : listViews.size();
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
        Log.d("pageradapter:", String.valueOf(position)+" is instantiated");
        ((ViewPager) container).addView(mViewList.get(position % size),0);
        return mViewList.get(position % size);
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
        ((ViewPager) container).removeView(mViewList.get(position% size));
        Log.d("pageradapter:", String.valueOf(position)+" is destoried");
//        container.removeView(mViewList.get(position % mViewList.size()));
        //container.removeView(mViewList.get(position));
    }
}
