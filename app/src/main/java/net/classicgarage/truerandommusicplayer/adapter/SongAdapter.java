package net.classicgarage.truerandommusicplayer.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.LinkedList;


/**
 * Created by Tong on 2017/5/16.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private LinkedList<SongItem> mSongs;
    private OnSongItemNameClickListener mOnSongItemNameClickListener;
//    private OnFavBtnClickListener mOnFavBtnClickListener;
//    private OnDelBtnClickListener mOnDelBtnClickListener;
    private SongDataSource mSongDataSource;
    private View mItemView;
    private Context mContext;

    public SongAdapter(Context context, LinkedList<SongItem> songs){
        mSongs = songs;
        mContext = context;
        mSongDataSource = SongDataSource.getInstance(context);
    }
//
//    /**
//     *  Used for defining method that used when items in the recycler view is clicked.
//     */
//
//
//    // create three interface for each item need ClickListener
    public interface OnSongItemNameClickListener {
        /**
         * Called when the item is clicked.
         * @param view
         * @param position
         */
        void onSongItemNameClick(View view, int position);
    }
//    public interface OnFavBtnClickListener {
//        void onFavBtnClick(View view, int position);
//    }
//    public interface OnDelBtnClickListener {
//        void onDelBtnClick(View view, int position);
//
//    }


    /**
     * Set the value of mOnSongNameClickListener, mOnFavBtnClickListener, mOnDelBtnClickListener.
     * @param listener
     */
    public void setOnSongItemNameClickListener(OnSongItemNameClickListener listener) {
        mOnSongItemNameClickListener = listener;

    }
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
        final long songId = song.getId();
        holder.mSongTitleTv.setText( song.getTitle() );
        if(song.getFavorite()) holder.mFavBtn.setImageResource(R.mipmap.fav_on);
        else holder.mFavBtn.setImageResource(R.mipmap.fav_off);
        holder.mFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSongDataSource.setSongFavorite( songId );
                notifyDataSetChanged();
            }
        });
        holder.mSongItemNameLLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mOnSongItemNameClickListener != null){
                    Log.d("====songname==", position+"");
                    mOnSongItemNameClickListener.onSongItemNameClick(holder.itemView, position);
                }
            }
        });
        holder.mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSongDataSource.deletSong( songId );
                notifyDataSetChanged();
            }
        });

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

    public void deletSong(SongItem songDeleting){
        for (int i = 0; i < mSongs.size();i++){
            if(mSongs.get(i) == songDeleting){
                mSongs.remove(i);

                //deletePlaylistTracks(this,)
            }
        }
    }

    public int deletePlaylistTracks(Context context, long playlistId, long audioId){
        ContentResolver resolver = context.getContentResolver();
        int countDel = 0;
        try{
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                    "external",playlistId);
            String where = MediaStore.Audio.Playlists.Members._ID + "=?";
            String audioId1 = Long.toString(audioId);
            String[] whereVal = { audioId1 };
            countDel = resolver.delete(uri,where,whereVal);
            Log.d("TAG", "tracks deleted=" + countDel);
        }catch (Exception e){
            Log.d("Error", "Error");
        }
        return countDel;
    }



    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mSongTitleTv;
        private LinearLayout mSongItemLlayout;
        private LinearLayout mSongItemNameLLayout;
        private ImageButton mFavBtn;
        private ImageButton mDelBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            mSongTitleTv = (TextView) itemView.findViewById(R.id.song_title_tv);
//            mSongItemLlayout = (LinearLayout) itemView.findViewById(R.id.song_item_llayout);
            mSongItemNameLLayout = (LinearLayout) itemView.findViewById(R.id.song_item_name_llayout);
            mFavBtn = (ImageButton) itemView.findViewById(R.id.song_item_fav_btn);
            mDelBtn = (ImageButton) itemView.findViewById(R.id.song_item_del_btn);
        }

    }
}