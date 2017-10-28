package net.classicgarage.truerandommusicplayer.listener;

import android.support.v7.widget.RecyclerView;

/**
 * Created by eaton on 27/10/2017.
 */

public abstract class OnItemClickListener {

    public void onItemLongClick(RecyclerView.ViewHolder vh, int position){}
    abstract public void onItemClick(RecyclerView.ViewHolder vh, int postion);
}
