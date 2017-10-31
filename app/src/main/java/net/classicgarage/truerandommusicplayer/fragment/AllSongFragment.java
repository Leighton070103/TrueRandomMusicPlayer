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
import net.classicgarage.truerandommusicplayer.util.PinnedHeaderDecoration;
import net.classicgarage.truerandommusicplayer.view.WaveSideBarView;

import static android.app.Activity.RESULT_OK;
import static net.classicgarage.truerandommusicplayer.fragment.FavSongFragment.PLAY_MODE;
import static net.classicgarage.truerandommusicplayer.service.MusicService.NORMAL_MODE;


public class AllSongFragment extends Fragment {

    private RecyclerView mSongListRv;
    private SongAdapter mAdapter;
    private SongDataSource mSongDataSource;
    private WaveSideBarView mSideBarView;

    public AllSongFragment() {

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
        View v = inflater.inflate(R.layout.fragment_all_music, container, false);
        mSongDataSource = SongDataSource.getInstance(getContext().getApplicationContext());
        super.onCreate(savedInstanceState);
        mSongListRv = (RecyclerView) v.findViewById(R.id.activity_all_music_rv);
        mSideBarView = (WaveSideBarView) v.findViewById(R.id.side_bar_view);

        mSongListRv.setLayoutManager(new LinearLayoutManager(getContext()
                .getApplicationContext()));

        final PinnedHeaderDecoration decoration = new PinnedHeaderDecoration();
        decoration.registerTypePinnedHeader(1, new PinnedHeaderDecoration.PinnedHeaderCreator() {
            @Override
            public boolean create(RecyclerView parent, int adapterPosition) {
                return true;
            }
        });
        mSongListRv.addItemDecoration(decoration);
        mSongListRv.setItemAnimator( new DefaultItemAnimator() );
        mAdapter = new SongAdapter(getContext().getApplicationContext(),
                mSongDataSource.getAllSongs());

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
        mAdapter.setOnLoadingMoreListener(new SongAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadingMore() {
//                if (!mLoading) {
//                    mLoading = true;
//                    notifyDataSetChanged();
//                }
            }
        });
        mSongListRv.setAdapter(mAdapter);

        mSideBarView.setOnTouchLetterChangeListener(new WaveSideBarView.OnTouchLetterChangeListener() {
            @Override
            public void onLetterChange(String letter) {
                int pos = mAdapter.getLetterPosition(letter);
                if(pos != -1) {
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
