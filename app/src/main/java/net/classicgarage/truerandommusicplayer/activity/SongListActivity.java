package net.classicgarage.truerandommusicplayer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TabHost;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.adapter.SongAdapter;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.ArrayList;

public class SongListActivity extends AppCompatActivity {
    private RecyclerView mSongListRv;
    private SongAdapter mAdapter;
    private TabHost mTabHost;
    private SongDataSource mSongDataSource;
    public static final String SONG_POSITION = "Song position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        getPermissons();
        mSongDataSource = new SongDataSource(this.getApplicationContext(), this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        mSongListRv = (RecyclerView) findViewById(R.id.song_list_rv);
        mAdapter = new SongAdapter(this, mSongDataSource.getSongsFromSD());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mSongListRv.setLayoutManager(mLayoutManager);
        mSongListRv.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent();
                intent.putExtra(SONG_POSITION, position);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mSongListRv.setAdapter(mAdapter);

//        mTabHost = (TabHost) findViewById(R.id.tabHost);
//
//        TabHost.TabSpec allMusicTap = mTabHost.newTabSpec("AllMusic");
//        TabHost.TabSpec favoriteTap = mTabHost.newTabSpec("Favorite");
//
//        allMusicTap.setIndicator("ALLMUSIC");
//        favoriteTap.setIndicator("FAVORITE");
//
//        mTabHost.addTab(allMusicTap);
//        mTabHost.addTab(favoriteTap);
//

    }

//    private void getPermissons() {
//        int code = ActivityCompat.checkSelfPermission(
//                SongListActivity.this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (code != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(SongListActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
//    }
}
