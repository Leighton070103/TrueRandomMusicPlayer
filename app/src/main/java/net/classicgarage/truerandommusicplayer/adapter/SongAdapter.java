package net.classicgarage.truerandommusicplayer.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.activity.MainActivity;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.LinkedList;


/**
 * This class is to provide basic data and view operations for the recycler view of the song lists.
 * Created by Tong on 2017/5/16.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private LinkedList<SongItem> mSongs;
    private OnSongItemNameClickListener mOnSongItemNameClickListener;
    private SongDataSource mSongDataSource;
    private View mItemView;
    private Context mContext;

    public SongAdapter(Context context, LinkedList<SongItem> songs){
        mSongs = songs;
        mContext = context;
        mSongDataSource = SongDataSource.getInstance(context);
    }

//    /**
//     *  Used for defining method that used when items in the recycler view is clicked.
//     */

//    // create three interface for each item need ClickListener
    public interface OnSongItemNameClickListener {
        /**
         * Called when the item is clicked.
         * @param view
         * @param position
         */
        void onSongItemNameClick(View view, int position);
    }

    /**
     * Set the value of mOnSongNameClickListener, mOnFavBtnClickListener, mOnDelBtnClickListener.
     * @param listener
     */
    public void setOnSongItemNameClickListener(OnSongItemNameClickListener listener) {
        mOnSongItemNameClickListener = listener;

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
        final long songId = song.getId();
        song.setSequenceLog(position + 1);
        holder.mSongSequenceNumberTv.setText(Integer.toString(song.getSequenceLog()));
        holder.mSongTitleTv.setText( song.getTitle());
        if(song.getFavorite()) holder.mFavBtn.setBackgroundResource(R.mipmap.fav_on);
        else holder.mFavBtn.setBackgroundResource(R.mipmap.fav_off);
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
               deleteDialog(songId);
            }
        });
        holder.mArtistTv.setText( song.getArtist() );

    }


    /**
     * To display a delete dialog.
     */
    protected void deleteDialog(final long songId){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.delete_alert);
        builder.setTitle(R.string.alert);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MediaScannerConnection.scanFile(mContext.getApplicationContext(), new String[]
                        { Environment.getExternalStorageDirectory().getAbsolutePath()}, null, null);
                mSongDataSource.deleteSong( songId );
                notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }



    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mSongSequenceNumberTv;
        private TextView mSongTitleTv;
        private TextView mArtistTv;
        private LinearLayout mSongItemNameLLayout;
        private ToggleButton mFavBtn;
        private ImageButton mDelBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            mSongSequenceNumberTv = (TextView) itemView.findViewById(R.id.song_item_number_tv);
            mSongTitleTv = (TextView) itemView.findViewById(R.id.song_item_title_tv);
            mArtistTv = (TextView) itemView.findViewById(R.id.song_item_artist_tv);
            mSongItemNameLLayout = (LinearLayout) itemView.findViewById(R.id.song_item_name_llayout);
            mFavBtn = (ToggleButton) itemView.findViewById(R.id.song_item_fav_btn);
            mDelBtn = (ImageButton) itemView.findViewById(R.id.song_item_del_btn);
        }

    }
}