package net.classicgarage.truerandommusicplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
//    private OnSongNameClickListener mOnSongNameClickListener;
//    private OnFavBtnClickListener mOnFavBtnClickListener;
//    private OnDelBtnClickListener mOnDelBtnClickListener;
    private View mItemView;
    private Context mContext;

    public SongAdapter(Context context, ArrayList<SongItem> songs){
        mSongs = songs;
        mContext = context;
    }
//
//    /**
//     *  Used for defining method that used when items in the recycler view is clicked.
//     */
//
//
//    // create three interface for each item need ClickListener
//    public interface OnSongNameClickListener {
//        /**
//         * Called when the item is clicked.
//         * @param view
//         * @param position
//         */
//        void onSongNameClick(View view, int position);
//    }
//    public interface OnFavBtnClickListener {
//        void onFavBtnClick(View view, int position);
//    }
//    public interface OnDelBtnClickListener {
//        void onDelBtnClick(View view, int position);
//
//    }


//    /**
//     * Set the value of mOnSongNameClickListener, mOnFavBtnClickListener, mOnDelBtnClickListener.
//     * @param listener
//     */
//    public void setOnSongNameClickListener(OnSongNameClickListener listener) {
//        mOnSongNameClickListener = listener;
//
//    }
//    public void setmOnFavBtnClickListener(OnFavBtnClickListener listener) {
//        mOnFavBtnClickListener = listener;
//    }
//    public void setmOnDelBtnClickListener(OnDelBtnClickListener listener) {
//        mOnDelBtnClickListener = listener;
//    }




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
//        holder.songNameLLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if( mOnSongNameClickListener != null){
//                    Log.d("====songname==", position+"");
//                    mOnSongNameClickListener.onSongNameClick(holder.itemView, position);
//                }
//            }
//        });
//        holder.delBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if( mOnDelBtnClickListener != null){
//                    Log.d("====del==", position+"");
//                    mOnDelBtnClickListener.onDelBtnClick(holder.itemView, position);
//                }
//            }
//        });
//        holder.favBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if( mOnFavBtnClickListener != null){
//                    Log.d("===fav===", position+"");
//                    mOnFavBtnClickListener.onFavBtnClick(holder.itemView, position);
//                }
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mSongTitleTv;
        private LinearLayout mSongItemLlayout;
        private LinearLayout songNameLLayout;
        private ImageButton favBtn;
        private ImageButton delBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            mSongTitleTv = (TextView) itemView.findViewById(R.id.song_title_tv);
            mSongItemLlayout = (LinearLayout) itemView.findViewById(R.id.song_item_llayout);
            songNameLLayout = (LinearLayout) itemView.findViewById(R.id.some_name);
            favBtn = (ImageButton) itemView.findViewById(R.id.fav_btn);
            delBtn = (ImageButton) itemView.findViewById(R.id.del_btn);
        }

    }
}