package net.classicgarage.truerandommusicplayer.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

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


        ((ViewPager) container).addView(mViewList.get(position % size),0);
        return mViewList.get(position % size);
        /*if(container.getParent()!=null) {
            ((ViewGroup)container.getParent()).removeView(mViewList.get(position));
        }
        container.addView(mViewList.get(position));
        return mViewList.get(position);

        container.addView(mViewList.get(position));
        container.addView(mViewList.get(position % mViewList.size()));
        return mViewList.get(position % mViewList.size());*/
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
//        container.removeView(mViewList.get(position % mViewList.size()));
        ((ViewPager) container).removeView(mViewList.get(position% size));
        //container.removeView(mViewList.get(position));
    }

}
