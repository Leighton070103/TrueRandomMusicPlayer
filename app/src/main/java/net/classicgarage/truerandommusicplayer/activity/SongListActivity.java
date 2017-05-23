package net.classicgarage.truerandommusicplayer.activity;

import android.Manifest;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.adapter.SongAdapter;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.db.SongDatabaseHelper;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import static android.R.id.tabhost;
import static net.classicgarage.truerandommusicplayer.R.id.AllMusic;
import static net.classicgarage.truerandommusicplayer.R.id.Favorite;

public class SongListActivity extends TabActivity {
    private RecyclerView mSongListRv;
    private SongAdapter mAdapter;
    private TabHost mTabHost;
    private SongDataSource mSongDataSource;
    private SongDatabaseHelper mSongDataBase;
    //private TestSongTagHelper mTestSongTagHelper = new TestSongTagHelper();
    //private ArrayList<SongItem> mTestSongs;
    public static final String SONG_POSITION = "songPosition";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        getPermissons();
        mSongDataSource = SongDataSource.getInstance(this.getApplicationContext());
        mSongDataBase = SongDatabaseHelper.getInstance(this.getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        mSongListRv = (RecyclerView) findViewById(R.id.song_list_rv);
        mTabHost = (TabHost) findViewById(tabhost);
        mTabHost.setup();
        mTabHost.addTab(mTabHost.newTabSpec("ALLMUSIC").setIndicator("AllMusic").setContent(R.id.AllMusic));
        mTabHost.addTab(mTabHost.newTabSpec("FAVORITE").setIndicator("Favorite").setContent(R.id.Favorite));
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                switch(mTabHost.getCurrentTab()){
                    case AllMusic:

                        break;
                    case Favorite:

                        break;
                    default:
                }
            }
        });
        mAdapter = new SongAdapter(this,mSongDataSource.getSongsFromSD());
        //mAdapter = new SongAdapter(this, mSongDataSource.getSongsFromSD());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mSongListRv.setLayoutManager(mLayoutManager);
        mSongListRv.setItemAnimator(new DefaultItemAnimator());

        // set three click listner for:song name, favBtn, Delbtn
        mAdapter.setOnSongNameClickListener(new SongAdapter.OnSongNameClickListener() {
            @Override
            public void onSongNameClick(View view, int position) {
                Intent intent = new Intent();
                intent.putExtra(SONG_POSITION, position);
                setResult(RESULT_OK, intent);
                finish();
                Log.d("===songlist===", "song clicked");

                // direct to MainActivity with song position
//                Intent intent = new Intent(SongListActivity.this, MainActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putInt("songPosition", position);
//                intent.putExtras(bundle);
//                startActivity(intent);
            }

        });
        mAdapter.setmOnFavBtnClickListener(new SongAdapter.OnFavBtnClickListener() {
            @Override
            public void onFavBtnClick(View view, int position) {
                Log.d("===songlist===", "favBtn clicked");

                // change clicked item statu in datasource
                mSongDataSource.setSongItemFavStatuAtPosition(true, position);
                //add songItem to favDataBase
                SongItem favItem = mSongDataSource.getSongAtPosition(position);
                mSongDataBase.addFavoriteSong(favItem);
                // TODO: change favBtn image
            }

        });
        mAdapter.setmOnDelBtnClickListener(new SongAdapter.OnDelBtnClickListener() {
            @Override
            public void onDelBtnClick(View view, int position) {
                Log.d("===songlist===", "favBtn clicked");
                // TODO: do something when DelBtn be clicked
                SongItem delItem = mSongDataSource.getSongAtPosition(position);
            }

        });
        mSongListRv.setAdapter(mAdapter);


        TabHost.TabSpec allMusicTap = mTabHost.newTabSpec("AllMusic");
        TabHost.TabSpec favoriteTap = mTabHost.newTabSpec("Favorite");

        allMusicTap.setIndicator("ALLMUSIC");
        favoriteTap.setIndicator("FAVORITE");
    }

    private void getPermissons() {
        int code = ActivityCompat.checkSelfPermission(
                SongListActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (code != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SongListActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
}
