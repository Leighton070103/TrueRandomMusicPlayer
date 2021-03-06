package net.classicgarage.truerandommusicplayer.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.activity.SongListActivity;
import net.classicgarage.truerandommusicplayer.adapter.SongAdapter;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.view.WaveSideBarView;

import static android.app.Activity.RESULT_OK;
import static net.classicgarage.truerandommusicplayer.service.MusicService.FAV_MODE;


public class FavSongFragment extends Fragment {

    private RecyclerView mSongListRv;
    private SongAdapter mAdapter;
    private SongDataSource mSongDataSource;
    private WaveSideBarView mSideBarView;
    public static final String PLAY_MODE = "play mode";

    public FavSongFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite, container, false);
        mSongDataSource = SongDataSource.getInstance(this.getContext().getApplicationContext());
        super.onCreate(savedInstanceState);
        mSongListRv = (RecyclerView) v.findViewById(R.id.fav_song_list_rv);
        mSideBarView = (WaveSideBarView) v.findViewById(R.id.side_bar_view);

        mAdapter = new SongAdapter(this.getContext(), mSongDataSource.getFavoriteSongs());
        //mAdapter = new SongAdapter(this, mSongDataSource.getAllSongs());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext()
                .getApplicationContext());
        mSongListRv.setLayoutManager(mLayoutManager);
        mSongListRv.setItemAnimator(new DefaultItemAnimator());

        mAdapter.setOnSongItemNameClickListener(new SongAdapter.OnSongItemNameClickListener() {
            @Override
            public void onSongItemNameClick(View view, int position) {
                Intent result = new Intent();
                result.putExtra(SongListActivity.SONG_POSITION, position);
                result.putExtra(PLAY_MODE, FAV_MODE);
                getActivity().setResult(RESULT_OK, result);
                getActivity().finish();
            }
        });
        mSongListRv.setAdapter(mAdapter);

        mSideBarView.setOnTouchLetterChangeListener(new WaveSideBarView.OnTouchLetterChangeListener() {
            @Override
            public void onLetterChange(String letter) {
                int pos = mAdapter.getLetterPosition(letter);
                if(pos != -1){
                    mSongListRv.scrollToPosition(pos);
                    LinearLayoutManager mLayoutManager =
                            (LinearLayoutManager) mSongListRv.getLayoutManager();
                    mLayoutManager.scrollToPositionWithOffset(pos,0);
                }
            }
        });
        return v;
    }

}
