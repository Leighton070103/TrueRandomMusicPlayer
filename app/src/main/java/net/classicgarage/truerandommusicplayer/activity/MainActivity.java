package net.classicgarage.truerandommusicplayer.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.adapter.SwipePagerAdapter;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;
import net.classicgarage.truerandommusicplayer.service.BaseService;
import net.classicgarage.truerandommusicplayer.service.MusicService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //, OnSharedPreferenceChangeListener, SensorEventListener {

    public static final int ALBUM_ART_HEIGHT = 150;
    public static final int ALBUM_ART_WIDTH = 150;
    public static final int REQUEST_CODE = 070103;

    public static final int REQUEST_PICK_SONG = 0; 	// used for calling SongPicker activity

    ImageButton mPlayPauseBtn;
    ImageButton mRandomBtn;
    ImageButton mSkipBtn;
    ImageButton mRewBtn;
    ImageButton mStopBtn;
    ImageButton mFavoriteBtn;
    ImageButton mFilePickerBtn;
    ImageButton mPlayListBtn;
    ImageButton mDeleteBtn;
    static SeekBar sProgressBar;
    TextView mSongTitleTv;
    ImageView mAlbumArtIv;
    static TextView timeSpendTv;
    static TextView totalTimeTv;

//    FlingGalleryView flingGalleryView;
    private ViewPager viewPager;
    private List<View> albumImageViewList;
    private SwipePagerAdapter swipePagerAdapter;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private static SimpleDateFormat time = new SimpleDateFormat("mm:ss");

    SongItem songPlaying;

    private ServiceConnection mMusicConn;
    private BaseService mBaseService;
    private SongDataSource mSongDataSource;


    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int duration = bundle.getInt("duration");
            int position = bundle.getInt("position");

            timeSpendTv.setText(time.format(position));
            totalTimeTv.setText(time.format(duration));

            sProgressBar.setMax(duration);
            sProgressBar.setProgress(position);

        }
    };
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSongDataSource = SongDataSource.getInstance(this.getApplicationContext());

        getPermissons();
        mSongTitleTv = (TextView) findViewById(R.id.title_tv);
      //  mSongTitleTv.setText(mBaseService.getPlayingSong().getTitle());
//        mAlbumArtIv = (ImageView) findViewById(R.id.cover_iv);
        mPlayPauseBtn = (ImageButton) findViewById(R.id.play_pause_btn);
//        mRandomBtn = (ImageButton) findViewById(R.id.random_btn);
//        mSkipBtn = (ImageButton) findViewById(R.id.next_btn);
//        mRewBtn = (ImageButton) findViewById(R.id.pre_btn);
        mPlayListBtn = (ImageButton) findViewById(R.id.playlist_btn);
        sProgressBar = (SeekBar) findViewById(R.id.procress_bar);
        mSkipBtn = (ImageButton) findViewById(R.id.next_btn);
        timeSpendTv = (TextView) findViewById(R.id.timespend_tv);
        totalTimeTv = (TextView) findViewById(R.id.totaltime_tv);

        mPlayPauseBtn.setOnClickListener(this);
//        mSkipBtn.setOnClickListener(this);
//        mRewBtn.setOnClickListener(this);
//        mRandomBtn.setOnClickListener(this);
        mPlayListBtn.setOnClickListener(this);
        mSkipBtn.setOnClickListener(this);

        sProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    mBaseService.callSeekTo(sProgressBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });



        Intent intent = new Intent(MainActivity.this, MusicService.class);
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

        initSwipeView();


    }

    public void initSwipeView() {
        albumImageViewList = new ArrayList<View>();
        viewPager = (ViewPager) findViewById(R.id.swipe_viewpager);

        LayoutInflater inflater=getLayoutInflater();
        for(int i = 0;  i < mSongDataSource.getSongsFromSD().size(); i++) {
            View album_view = inflater.inflate(R.layout.album_img_layout, null);
            albumImageViewList.add(album_view);
        }


         swipePagerAdapter = new SwipePagerAdapter(albumImageViewList);

        final int currentItem = Integer.MAX_VALUE / 2;

        viewPager.setCurrentItem(currentItem);
        viewPager.setAdapter(swipePagerAdapter);

        mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
            public int currentPos;
            @Override
            /**
             * position:current page;
              positionOffset: offset of current page
              positionOffsetPixels: offset pixels of current page
              */
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPos = position;
            }

            @Override
            public void onPageSelected(int position) {
                // play song at the position when swipe to another page
                mBaseService.callPlaySongAtPosition(position);
                mBaseService.callSeekTo(0);
                Log.d("*******", position+"");
            }

            @Override
             /**
              * state == ViewPager.SCROLL_STATE_DRAGGING ||  ViewPager.SCROLL_STATE_SETTLING
              * || ViewPager.SCROLL_STATE_IDLE
              * */
            public void onPageScrollStateChanged(int state) {}
        };
        viewPager.addOnPageChangeListener(mOnPageChangeListener);
    }


    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onPause() {
         super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    public void onClick(View v) {

        switch (v.getId()){
            case R.id.play_pause_btn:
                if(mBaseService.isPlaying()){
                    mBaseService.callPause();
                    mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.mipmap.play_btn));
                }
                else {
                    mBaseService.callPlay();
                    mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.mipmap.pause_btn));
                }
                break;
            case R.id.playlist_btn:
                Intent i = new Intent(this,SongListActivity.class);
                startActivityForResult(i, REQUEST_CODE);
                break;
            case R.id.next_btn:
                mBaseService.callPause();
                mBaseService.callPlayNextSong();
                mSongTitleTv.setText(mBaseService.getPlayingSong().getTitle());
                break;
        }
    }

    /**
     * update buttons
     */
    private void updateButtonDisplay () {

    }

    /*
     * receive result from SongPickerActivity
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_CANCELED) return;
        Bundle extras = intent.getExtras();
        int position = extras.getInt("songPosition");
        mBaseService.callPlaySongAtPosition(position);

        if (requestCode == REQUEST_CODE) {

        }
    }


    /*
     * inner broadcast receiver class
     */
//    class PlayerStatusReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive (Context context, Intent intent) {
//        }
//    }

    // start menu first time user press on the "menu" touch
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater ();
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//
//        return true;
//    }

//    //handle menu options
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        return true;
//    }

    private void getPermissons() {
        int code = ActivityCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (code != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(mMusicConn);
    }

//    class MusicConn implements ServiceConnection {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            mBaseService = (BaseService) service;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//        }
//    }

            final Handler mHandler = new Handler();
        final Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                timeSpendTv.setText(time.format(mBaseService.callGetCurrentPosition()));
                totalTimeTv.setText(time.format(mBaseService.callGetDuration()));

                sProgressBar.setProgress(mBaseService.callGetCurrentPosition());
                sProgressBar.setMax(mBaseService.callGetDuration());

                sProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser) {
                            mBaseService.callSeekTo(sProgressBar.getProgress());
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                //mHandler.postDelayed(this, 100);
            }
        };


}
