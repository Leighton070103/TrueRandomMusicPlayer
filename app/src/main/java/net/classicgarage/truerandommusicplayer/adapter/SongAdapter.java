package net.classicgarage.truerandommusicplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.model.SongItem;
import java.util.ArrayList;


/**
 * Created by Tong on 2017/5/16.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private ArrayList<SongItem> mSongs;
    private View mItemView;
    private Context mContext;

    public SongAdapter(Context context, ArrayList<SongItem> songs){
        mSongs = songs;
        mContext = context;
    }
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mItemView = LayoutInflater.from( parent.getContext() ).inflate(R.layout.song_item,
                parent, false);
        return new ViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(SongAdapter.ViewHolder holder, int position) {
        SongItem song = mSongs.get(position);
        holder.mSongTitleTv.setText( song.getTitle() );

    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mSongTitleTv;

        public ViewHolder(View itemView) {
            super(itemView);
            mSongTitleTv = (TextView) itemView.findViewById(R.id.item_song_title_tv);
        }
    }
}