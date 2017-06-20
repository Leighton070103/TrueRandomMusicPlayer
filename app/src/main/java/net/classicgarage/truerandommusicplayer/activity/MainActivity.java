package net.classicgarage.truerandommusicplayer.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.adapter.SwipePagerAdapter;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;
import net.classicgarage.truerandommusicplayer.service.BaseService;
import net.classicgarage.truerandommusicplayer.service.MusicService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This activity is to provide the main view for this app.
 * And enable users to check the song lists, play, pause, delete music, or label them as favorite.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 070103;

    /**
     * Used for calling SongPicker activity
     */


    public boolean musicFlag = false; // use for music running or not
    public boolean playFlag = false;
    public boolean replayFlag = false;
    public boolean randomFlag = false;
    ImageButton mPlayPauseBtn;
    ImageView mAlbumArtView;
    ImageButton mRandomBtn;
    ImageButton mReplayBtn;
    ImageButton mNextBtn;
    ImageButton mPreBtn;
    ImageButton mStopBtn;
    ToggleButton mFavoriteBtn;
    ImageButton mFilePickerBtn;
    ImageButton mPlayListBtn;
    ImageButton mDeleteBtn;
    static SeekBar sSeekBar;
    TextView mSongTitleTv;
    TextView mAuthorTv;
    TextView mAlbumTv;
    ImageView mAlbumArtIv;
    TextView mSongTimeTv;
    static TextView mSongLeftTimeTv;

    private ServiceConnection mMusicConn;
    private BaseService mBaseService;
    private SongDataSource mSongDataSource;

    private ViewPager mViewPager;
    private List<View> mAlbumImageViewList;
    private SwipePagerAdapter mSwipePagerAdapter;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private static SimpleDateFormat time = new SimpleDateFormat("mm:ss");

    public static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int duration = bundle.getInt("duration");
            int position = bundle.getInt("position");
            sSeekBar.setMax(duration);
            sSeekBar.setProgress(position);
            mSongLeftTimeTv.setText(SongItem.formateTime(position)+"");

        }
    };

    /**
     * Called when the activity is first created.
     **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSongDataSource = SongDataSource.getInstance(this.getApplicationContext());

        mSongLeftTimeTv = (TextView) findViewById(R.id.timespend_tv);
        mSongTitleTv = (TextView) findViewById(R.id.title_tv);
        mAuthorTv = (TextView) findViewById(R.id.author_tv);
        mAlbumTv = (TextView) findViewById(R.id.album_tv);
        mAuthorTv = (TextView) findViewById(R.id.author_tv);
        mDeleteBtn = (ImageButton) findViewById(R.id.activity_main_delete_btn);
        mFavoriteBtn = (ToggleButton) findViewById(R.id.favorite_btn);
        mPlayPauseBtn = (ImageButton) findViewById(R.id.play_pause_btn);
        mPreBtn = (ImageButton) findViewById(R.id.pre_btn);
        mPlayListBtn = (ImageButton) findViewById(R.id.playlist_btn);
        sSeekBar = (SeekBar) findViewById(R.id.procress_bar);
        mNextBtn = (ImageButton) findViewById(R.id.next_btn);
        mSongTimeTv = (TextView) findViewById(R.id.timeleft_tv);
        mRandomBtn = (ImageButton) findViewById(R.id.random_btn);
        mReplayBtn = (ImageButton) findViewById(R.id.replay_btn);

        mPlayPauseBtn.setOnClickListener(this);
        mPreBtn.setOnClickListener(this);
        mPlayListBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);
        mFavoriteBtn.setOnClickListener(this);
        mRandomBtn.setOnClickListener(this);
        mReplayBtn.setOnClickListener(this);

        getPermissions();
    }

    /**
     * This method is check permissions which is necessary for this app, and if the permission is
     * not granted, request it from the user.
     */
    private void getPermissions() {
        int code = ActivityCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (code != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else{
            setSeekBarListener();
            startServices();
            enableSwiping();
        }
    }

    /**
     * This method is called when a request of permission is finished.
     * And if the app has the permission, enable the basic functions.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                //seek bar
                setSeekBarListener();
                // app service
                startServices();
                //swipe
                enableSwiping();
        }
    }

    /**
     * This method is to start the music service.
     */
    private void startServices(){
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);
        mMusicConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBaseService = (BaseService) service;
                updateMainPage();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        getApplicationContext().bindService(intent, mMusicConn, BIND_AUTO_CREATE);
    }

    /**
     * This method is to init the swipe view, so as to enable the swiping to change song function.
     */
    private void enableSwiping(){
        try {
            initSwipeView();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is to set listener to the seek bar, change its status due to different operations.
     */
    private void setSeekBarListener(){
        //seek bar
        sSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSongLeftTimeTv.setText(SongItem.formateTime(progress));
                updateMainPage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mBaseService.isPlaying()) {
                    mBaseService.callPause();
                    mBaseService.callSeekTo(seekBar.getProgress());
                    mBaseService.callContinueMusic();
                }
                else{
                    mBaseService.callPause();
                    mBaseService.callSeekTo(seekBar.getProgress());
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    /**
     * Initialization of the swipe view.
     * @throws IllegalAccessException
     */
    public void initSwipeView() throws IllegalAccessException {
        mAlbumImageViewList = new ArrayList<View>();
        mViewPager = (ViewPager) findViewById(R.id.swipe_viewpager);

        LayoutInflater inflater = getLayoutInflater();
        for(int i = 0; i < mSongDataSource.getAllSongs().size(); i++) {
            View album_view = inflater.inflate(R.layout.album_img_layout, null);
            mAlbumArtView = (ImageView) album_view.findViewById(R.id.albumView);
            long songId = mSongDataSource.getAllSongs().get(i).getId();
            long album = mSongDataSource.getAllSongs().get(i).getAlbumId();
            Bitmap bitmap = SongItem.getArtwork(this.getApplicationContext(),songId,album,false);
            mAlbumArtView.setImageBitmap(bitmap);
            mAlbumImageViewList.add(album_view);
        }

        mSwipePagerAdapter = new SwipePagerAdapter(mAlbumImageViewList);

        final int currentItem = Integer.MAX_VALUE / 2;

        mViewPager.setCurrentItem(currentItem);
        mViewPager.setAdapter(mSwipePagerAdapter);

        mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
            public int currentPos;

            /**
             * position:current page;
             * positionOffset: offset of current page
             * positionOffsetPixels: offset pixels of current page
             */
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPos = position;
                updateMainPage();
                updateButtonDisplay();
            }

            @Override
            public void onPageSelected(int position) {
                // play song at the position when swipe to another page
                mBaseService.callPlaySongAtPosition(position);
                mBaseService.callSeekTo(0);
                updateMainPage();
                updateButtonDisplay();
                Log.d("*******", position+"");
            }

            @Override
             /**
              * state == ViewPager.SCROLL_STATE_DRAGGING ||  ViewPager.SCROLL_STATE_SETTLING
              * || ViewPager.SCROLL_STATE_IDLE
              */
            public void onPageScrollStateChanged(int state) {}
        };
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
    }

    /**
     * Called when the activity is displayed, and update the main page.
     */
    @Override
    public void onResume() {
        super.onResume();
        updateMainPage();
    }

    @Override
    public void onPause() {
         super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    /**
     * The onclick method for most all the buttons.
     * @param v
     */
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_pause_btn:
                mSongTitleTv.setText(mBaseService.getPlayingSong().getTitle());
                mSongTimeTv.setText(mBaseService.getPlayingSong().getSongTime());
                if(mBaseService.isPlaying()){
                    mBaseService.callPause();
                }
                else if(musicFlag){
                    mBaseService.callContinueMusic();
                }
                else{
                    mBaseService.callPlay();
                    mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.mipmap.pause_btn));
                }
                updateButtonDisplay();
                break;
            case R.id.playlist_btn:
                Intent i = new Intent(this,SongListActivity.class);
                startActivityForResult(i, REQUEST_CODE);
                break;
            case R.id.next_btn:
                //mBaseService.callPause();
                 mBaseService.callPlayNextSong();
                    updateMainPage();
                break;
            case R.id.pre_btn:
                //mBaseService.callPause();
                    mBaseService.callPlayLastSong();
                    updateMainPage();
                break;
            case R.id.activity_main_delete_btn:
                deleteDialog();
                break;
            case R.id.favorite_btn:
                mBaseService.setCurrentSongFavorite();
                break;
            case R.id.random_btn:
                updateRandomButton();
                break;
            case R.id.replay_btn:
                updateReplayButton();
                break;
        }
    }

    /**
     * To display a delete dialog.
     */
    protected void deleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure to delete this song?");
        builder.setTitle("Alert");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBaseService.deleteCurrentSong();
                MediaScannerConnection.scanFile(getApplicationContext(),new String[] {Environment.getExternalStorageDirectory().getAbsolutePath()}, null, null);
                updateMainPage();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Update the main page according to the current playing song.
     */
    public void updateMainPage(){
        if( mBaseService != null){
            SongItem song = mBaseService.getPlayingSong();
            if(  song != null) {
                mSongTitleTv.setText( song.getTitle());
                mAuthorTv.setText( song.getArtist());
                mAlbumTv.setText( song.getAlbum() );
                mSongTimeTv.setText(mBaseService.getPlayingSong().getSongTime());
            }
            else mSongTitleTv.setText("No music stored in this phone.");
            updateButtonDisplay();
        }
    }

    /**
     * update buttons
     */
    private void updateButtonDisplay () {
        if(mBaseService != null) {
            if (!mBaseService.isPlaying()) {
                mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.mipmap.play_btn));
            } else {
                mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.mipmap.pause_btn));
            }
        }
    }

    private void updateRandomButton() {
        if(!randomFlag) {
            mRandomBtn.setImageDrawable(getResources().getDrawable(R.mipmap.random1b));
            randomFlag = true;
            mBaseService.callChangeRandomFlag();
            if(replayFlag){
                mReplayBtn.setImageDrawable(getResources().getDrawable(R.mipmap.replay1w));
                replayFlag = false;
                mBaseService.callChangeReplayFlag();
            }
        }
        else {
            mRandomBtn.setImageDrawable(getResources().getDrawable(R.mipmap.randomw));
            randomFlag = false;
            mBaseService.callChangeRandomFlag();
        }
    }

    private void updateReplayButton() {
        if(!replayFlag) {
            mReplayBtn.setImageDrawable(getResources().getDrawable(R.mipmap.replay1b));
            replayFlag = true;
            mBaseService.callChangeReplayFlag();
            if(randomFlag){
                mRandomBtn.setImageDrawable(getResources().getDrawable(R.mipmap.randomw));
                randomFlag = false;
                mBaseService.callChangeRandomFlag();
            }
        }
        else {
            mReplayBtn.setImageDrawable(getResources().getDrawable(R.mipmap.replay1w));
            replayFlag = false;
            mBaseService.callChangeReplayFlag();
        }
    }

    /**
     * receiving results from songPickerActivity
     * @param requestCode
     * @param resultCode
     * @param intent
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(mMusicConn);
    }
}
