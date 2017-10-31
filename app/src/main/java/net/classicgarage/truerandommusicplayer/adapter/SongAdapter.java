package net.classicgarage.truerandommusicplayer.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.classicgarage.truerandommusicplayer.R;

import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.LinkedList;


/**
 * This class is to provide basic data and view operations for the recycler view of the song lists.
 * Created by Tong on 2017/5/16.
 */

public class SongAdapter extends BaseRecyclerViewAdapter<SongItem, SongAdapter.BaseViewHolder> {

    private LinkedList<SongItem> mSongs;
    private OnSongItemNameClickListener mOnSongItemNameClickListener;
    private SongDataSource mSongDataSource;
    private View mItemView;
    private Context mContext;

    public SongAdapter(Context context, LinkedList<SongItem> songs){
        super(context, songs);
        mSongDataSource = SongDataSource.getInstance(context);
    }

    /**
     *  Used for defining method that used when items in the recycler view is clicked.
     */

    // create three interface for each item which needs ClickListener
    public interface OnSongItemNameClickListener {

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
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item,
                parent, false);
        return super.onCreateViewHolder(parent,viewType);
    }

    @Override
    protected SongAdapter.BaseViewHolder onCreateDifViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 0)
            return new SongHolder(inflateItemView(R.layout.song_item,parent));
        else
            return new PinnedHolder(inflateItemView(R.layout.item_pinned_header,parent));
    }

    @Override
    protected void convert(final BaseViewHolder holder, final SongItem item) {
        if(holder instanceof SongHolder){
            ((SongHolder)holder).mSongTitleTv.setText(item.getTitle());
            if(item.getFavorite()){
                ((SongHolder)holder).mFavBtn.setBackgroundResource(R.mipmap.fav_on);
            }else{
                ((SongHolder)holder).mFavBtn.setBackgroundResource(R.mipmap.fav_off);
            }
            ((SongHolder)holder).mFavBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mSongDataSource.setSongFavorite(item.getId());
                    notifyDataSetChanged();
                }
            });
            ((SongHolder)holder).mSongItemNameLLayout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(mOnSongItemNameClickListener != null){
                        mOnSongItemNameClickListener.onSongItemNameClick(((SongHolder) holder).itemView,
                                getPositionFromSongs(item));
                    }
                }
            });
            ((SongHolder)holder).mDelBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    deleteDialog(item.getId());
                }
            });
            ((SongHolder)holder).mArtistTv.setText( item.getArtist() );
        }else{
            String letter = item.getTitle().substring(0,1);
            ((PinnedHolder)holder).songTip.setText(letter);
        }
    }

    @Override
    protected int getDefItemViewType(int position) {
        return super.getDefItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return getItems().size();
    }

    public int getLetterPosition(String letter){
        boolean otherCharactor = false;
        for(int i = 0; i < getItems().size(); i++){
            String newLetter = getItems().get(i).getTitle().substring(0,1);
            if(letter.equals(newLetter)){
                return i;
            }else if(i == getItems().size() && !letter.equals(newLetter)){
                return getItems().size();
            }
        }
        return -1;
    }

    /**
     * display a delete dialog
     * @param songId the song which needs to be deleted.
     */
    private void deleteDialog(final long songId){
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

    /**
     * get the position of one song within its list.
     * @param item the song that needs to be checked with its position.
     * @return an integer of position.
     */
    private int getPositionFromSongs(SongItem item){
        for(int i = 0; i < getItems().size(); i++){
            if(getItems().get(i) == item){
                return i;
            }
        }
        return -1;
    }

    public class SongHolder extends BaseViewHolder{

        private TextView mSongTitleTv;
        private TextView mArtistTv;
        private LinearLayout mSongItemNameLLayout;
        private ToggleButton mFavBtn;
        private ImageButton mDelBtn;

        public SongHolder(View itemView) {
            super(itemView);
            mSongTitleTv = (TextView) itemView.findViewById(R.id.song_item_title_tv);
            mArtistTv = (TextView) itemView.findViewById(R.id.song_item_artist_tv);
            mSongItemNameLLayout = (LinearLayout) itemView.findViewById(R.id.song_item_name_llayout);
            mFavBtn = (ToggleButton) itemView.findViewById(R.id.song_item_fav_btn);
            mDelBtn = (ImageButton) itemView.findViewById(R.id.song_item_del_btn);
        }
    }

    public class PinnedHolder extends BaseViewHolder{

        TextView songTip;

        public PinnedHolder(View itemView) {
            super(itemView);
            songTip = findViewById(R.id.song_tip);
        }
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder {

        private final SparseArray<View> mViews;

        public BaseViewHolder(View itemView) {
            super(itemView);
            this.mViews = new SparseArray<>();
        }

        public <T extends View> T findViewById(int viewId){
            View view = mViews.get(viewId);
            if(view==null){
                view=itemView.findViewById(viewId);
                mViews.put(viewId,view);
            }
            return (T) view;
        }
    }
}