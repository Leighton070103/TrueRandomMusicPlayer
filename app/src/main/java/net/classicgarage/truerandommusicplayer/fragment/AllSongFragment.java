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

import static android.app.Activity.RESULT_OK;
import static net.classicgarage.truerandommusicplayer.fragment.FavSongFragment.PLAY_MODE;
import static net.classicgarage.truerandommusicplayer.service.MusicService.NORMAL_MODE;


public class AllSongFragment extends Fragment {

    private RecyclerView mSongListRv;
    private SongAdapter mAdapter;
    private SongDataSource mSongDataSource;

    public AllSongFragment() {
        // Required empty public constructor
    }


    public static AllSongFragment newInstance() {
        AllSongFragment fragment = new AllSongFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_all_music, container, false);
        mSongDataSource = SongDataSource.getInstance(getContext().getApplicationContext());
        super.onCreate(savedInstanceState);
        mSongListRv = (RecyclerView) v.findViewById(R.id.activity_all_music_rv);

        mAdapter = new SongAdapter(getContext().getApplicationContext(),
                mSongDataSource.getAllSongs());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext()
                .getApplicationContext());
        mSongListRv.setLayoutManager(mLayoutManager);
        mSongListRv.setItemAnimator( new DefaultItemAnimator() );
        mAdapter.setOnSongItemNameClickListener(new SongAdapter.OnSongItemNameClickListener() {
            @Override
            public void onSongItemNameClick(View view, int position) {
                Intent result = new Intent();
                result.putExtra(SongListActivity.SONG_POSITION, position);
                result.putExtra(PLAY_MODE, NORMAL_MODE);
                getActivity().setResult(RESULT_OK, result);
                getActivity().finish();
            }
        });
        mSongListRv.setAdapter(mAdapter);
        return v;

    }
}
