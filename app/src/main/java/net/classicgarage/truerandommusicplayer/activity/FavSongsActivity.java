package net.classicgarage.truerandommusicplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.adapter.SongAdapter;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.service.BaseService;
import net.classicgarage.truerandommusicplayer.service.MusicService;

public class FavSongsActivity extends AppCompatActivity {

    private RecyclerView mSongListRv;
    private SongAdapter mAdapter;
    private SongDataSource mSongDataSource;
    private ServiceConnection mMusicConn;
    private BaseService mBaseService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mSongDataSource = SongDataSource.getInstance(this.getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        mSongListRv = (RecyclerView) findViewById(R.id.fav_song_list_rv);

        mAdapter = new SongAdapter(this,mSongDataSource.getFavoriteSongs());
        //mAdapter = new SongAdapter(this, mSongDataSource.getAllSongs());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mSongListRv.setLayoutManager(mLayoutManager);
        mSongListRv.setItemAnimator(new DefaultItemAnimator());
        mSongListRv.setAdapter(mAdapter);

        Intent intent = new Intent(FavSongsActivity.this, MusicService.class);
        startService(intent);
        mMusicConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBaseService = (BaseService) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        getApplicationContext().bindService(intent, mMusicConn, BIND_AUTO_CREATE);

        // set three click listner for:song name, favBtn, Delbtn
        mAdapter.setOnSongItemNameClickListener(new SongAdapter.OnSongItemNameClickListener() {
            @Override
            public void onSongItemNameClick(View view, int position) {
                long id = mSongDataSource.getFavoriteSongs().get(position).getId();
                mBaseService.callPlaySongAtPosition(mSongDataSource.findSongIndexById(id));
                finish();
            }
        });
    }

    /**
     * Update the recyclerview.
     */
    @Override
    protected void onResume() {
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        getApplicationContext().unbindService(mMusicConn);
        super.onDestroy();
    }
}
