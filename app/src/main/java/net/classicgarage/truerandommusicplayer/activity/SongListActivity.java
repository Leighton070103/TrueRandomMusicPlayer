package net.classicgarage.truerandommusicplayer.activity;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TabHost;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.service.BaseService;
import net.classicgarage.truerandommusicplayer.service.MusicService;

import static android.R.id.tabhost;

public class SongListActivity extends TabActivity {
  //  private RecyclerView mSongListRv;
//    private SongAdapter mAdapter;
    private TabHost mTabHost;
   // private SongDataSource mSongDataSource;
    private ServiceConnection mMusicConn;
    private BaseService mBaseService;
    private ImageButton mReturnBtn;
    public static final String SONG_POSITION = "songPosition";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

//
//
//        mSongDataSource = SongDataSource.getInstance(this.getApplicationContext());
        setContentView(R.layout.activity_song_list);
        super.onCreate(savedInstanceState);
        mReturnBtn = (ImageButton)findViewById(R.id.return_btn);
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongListActivity.this.finish();
            }
        });

        mTabHost = (TabHost) findViewById(tabhost);
        mTabHost.setup();
        TabHost.TabSpec allMusicTab = mTabHost.newTabSpec("All music");
        TabHost.TabSpec favoriteTab = mTabHost.newTabSpec("Favorite");

        allMusicTab.setIndicator("AllMusic");
        allMusicTab.setContent(new Intent(this,AllSongsActivity.class));
        favoriteTab.setIndicator("Favorite");
        favoriteTab.setContent(new Intent(this,FavSongsActivity.class));

        mTabHost.addTab(allMusicTab);
        mTabHost.addTab(favoriteTab);


//        mSongListRv = (RecyclerView) findViewById(R.id.song_list_rv);
//        mAdapter = new SongAdapter(this,mSongDataSource.getSongsFromSD());
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//        mSongListRv.setLayoutManager(mLayoutManager);
//        mSongListRv.setItemAnimator(new DefaultItemAnimator());

        Intent intent = new Intent(SongListActivity.this, MusicService.class);
        startService(intent);
        mMusicConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBaseService = (BaseService) service;
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        getApplicationContext().bindService(intent, mMusicConn, BIND_AUTO_CREATE);

        // set three click listner for:song name, favBtn, Delbtn
//        mAdapter.setOnSongItemNameClickListener(new SongAdapter.OnSongItemNameClickListener() {
//            @Override
//            public void onSongItemNameClick(View view, int position) {
//                mBaseService.callPlaySongAtPosition(position);
//                Log.d("===songlist===", "song clicked");
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        getApplicationContext().unbindService(mMusicConn);
        super.onDestroy();
    }
}
