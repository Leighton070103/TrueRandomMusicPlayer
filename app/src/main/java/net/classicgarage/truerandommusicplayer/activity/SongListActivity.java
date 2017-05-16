package net.classicgarage.truerandommusicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TabHost;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.adapter.SongAdapter;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.ArrayList;

public class SongListActivity extends AppCompatActivity {
    private RecyclerView mSongListRv;
    private SongAdapter mAdapter;
    private TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        mSongListRv = (RecyclerView) findViewById(R.id.song_list_rv);
        mAdapter = new SongAdapter(this, new ArrayList<SongItem>());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mSongListRv.setLayoutManager(mLayoutManager);
        mSongListRv.setItemAnimator(new DefaultItemAnimator());
        mSongListRv.setAdapter(mAdapter);

        mTabHost = (TabHost) findViewById(R.id.tabHost);

        TabHost.TabSpec allMusicTap = mTabHost.newTabSpec("AllMusic");
        TabHost.TabSpec favoriteTap = mTabHost.newTabSpec("Favorite");

        allMusicTap.setIndicator("ALLMUSIC");
        favoriteTap.setIndicator("FAVORITE");

        mTabHost.addTab(allMusicTap);
        mTabHost.addTab(favoriteTap);
        Intent updatesIntent = new Intent(this,);

        allMusicTap.setContent(updatesIntent);
    }
}
