package net.classicgarage.truerandommusicplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.ArrayList;


/**
 * Created by Tong on 2017/5/16.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private ArrayList<SongItem> mSongs;
    private OnItemClickListener mOnItemClickListener;
    private View mItemView;
    private Context mContext;

    public SongAdapter(Context context, ArrayList<SongItem> songs){
        mSongs = songs;
        mContext = context;
    }

    /**
     *  Used for defining method that used when items in the recycler view is clicked.
     */

    public interface OnItemClickListener {
        /**
         * Called when the item is clicked.
         * @param view
         * @param position
         */
        void onItemClick(View view, int position);
    }

    /**
     * Set the value of mOnItemClickListner.
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }



    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mItemView = LayoutInflater.from( parent.getContext() ).inflate(R.layout.song_item,
                parent, false);
        return new ViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(final SongAdapter.ViewHolder holder, final int position) {
        SongItem song = mSongs.get(position);
        holder.mSongTitleTv.setText( song.getTitle() );
        holder.mSongItemLlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mSongTitleTv;
        private LinearLayout mSongItemLlayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mSongTitleTv = (TextView) itemView.findViewById(R.id.song_title_tv);
            mSongItemLlayout = (LinearLayout) itemView.findViewById(R.id.song_item_llayout);
        }
    }
}