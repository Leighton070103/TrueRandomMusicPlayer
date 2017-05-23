package net.classicgarage.truerandommusicplayer.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.model.SongItem;
import net.classicgarage.truerandommusicplayer.service.BaseService;
import net.classicgarage.truerandommusicplayer.service.MusicService;

import javax.sql.DataSource;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //, OnSharedPreferenceChangeListener, SensorEventListener {

    public static final int ALBUM_ART_HEIGHT = 150;
    public static final int ALBUM_ART_WIDTH = 150;
    public static final int REQUEST_CODE = 070103;

    public static final int REQUEST_PICK_SONG = 0; 	// used for calling SongPicker activity
    public boolean musicFlag = false; // use for music running or not
    ImageButton mPlayPauseBtn;
    ImageButton mRandomBtn;
    ImageButton mNextBtn;
    ImageButton mPreBtn;
    ImageButton mStopBtn;
    ToggleButton mFavoriteBtn;
    ImageButton mFilePickerBtn;
    ImageButton mPlayListBtn;
    ImageButton mDeleteBtn;
    static ProgressBar sProgressBar;
    TextView mSongTitleTv;
    TextView mAuthorTv;
    TextView mAlbumTv;
    ImageView mAlbumArtIv;
    TextView mSongTimeTv;
    SongItem songPlaying;
    static TextView mSongLeftTimeTv;

    private ServiceConnection mMusicConn;
    private BaseService mBaseService;


    public static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int duration = bundle.getInt("duration");
            int position = bundle.getInt("position");
            sProgressBar.setMax(duration);
            sProgressBar.setProgress(position);
            mSongLeftTimeTv.setText(SongItem.formateTime(position)+"");
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissons();
        mSongLeftTimeTv = (TextView) findViewById(R.id.timespend_tv);
        mSongTitleTv = (TextView) findViewById(R.id.title_tv);
        mAuthorTv = (TextView) findViewById(R.id.author_tv);
        mAlbumTv = (TextView) findViewById(R.id.album_tv);
        mAuthorTv = (TextView) findViewById(R.id.author_tv);
        mDeleteBtn = (ImageButton) findViewById(R.id.activity_main_delete_btn);
        mFavoriteBtn = (ToggleButton) findViewById(R.id.favorite_btn);
//        mSongTitleTv.setText(mBaseService.getPlayingSong().getTitle());
//        mAlbumArtIv = (ImageView) findViewById(R.id.cover_iv);
        mPlayPauseBtn = (ImageButton) findViewById(R.id.play_pause_btn);
//        mRandomBtn = (ImageButton) findViewById(R.id.random_btn);
//        mSkipBtn = (ImageButton) findViewById(R.id.next_btn);
        mPreBtn = (ImageButton) findViewById(R.id.pre_btn);
        mPlayListBtn = (ImageButton) findViewById(R.id.playlist_btn);
        sProgressBar = (ProgressBar) findViewById(R.id.procress_bar);
        mNextBtn = (ImageButton) findViewById(R.id.next_btn);
        mSongTimeTv = (TextView) findViewById(R.id.timeleft_tv);


        mPlayPauseBtn.setOnClickListener(this);
        mPreBtn.setOnClickListener(this);
//        mRewBtn.setOnClickListener(this);
//        mRandomBtn.setOnClickListener(this);
        mPlayListBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);
        mFavoriteBtn.setOnClickListener(this);


        Intent intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);
        mMusicConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBaseService = (BaseService) service;
                updateMainPage();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        getApplicationContext().bindService(intent, mMusicConn, BIND_AUTO_CREATE);

//        updateMainPage();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }




    public void updateMainPage(){
        if( mBaseService != null){
            SongItem song = mBaseService.getPlayingSong();
            if(  song != null) {
                mSongTitleTv.setText( song.getTitle());
                mAuthorTv.setText( song.getArtist());
                mAlbumTv.setText( song.getAlbum() );
            }
            else mSongTitleTv.setText("No music stored in this phone.");
            updateButtonDisplay();
        }
    }

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

    public void onClick(View v) {
        mSongTimeTv.setText(mBaseService.getPlayingSong().getSongTime());
        switch (v.getId()){
            case R.id.play_pause_btn:
                mSongTitleTv.setText(mBaseService.getPlayingSong().getTitle());
                if(mBaseService.isPlaying()){
                    mBaseService.callPause();
                }
                else if(musicFlag){
                    mBaseService.callContinueMusic();
                }
                else{
                    musicFlag = true;
                    mBaseService.callPlay();
                }
                updateButtonDisplay();
                break;
            case R.id.playlist_btn:
                Intent i = new Intent(this,SongListActivity.class);
                startActivityForResult(i, REQUEST_CODE);
                break;
            case R.id.next_btn:
                mBaseService.callPause();
                mBaseService.callPlayNextSong();
                updateMainPage();
                break;
            case R.id.pre_btn:
                mBaseService.callPause();
                mBaseService.callPlayLastSong();
                updateMainPage();
                break;
            case R.id.activity_main_delete_btn:
                deleteDialog();
                break;
            case R.id.favorite_btn:
                mBaseService.setCurrentSongFavorite();


        }
    }

    protected void deleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure to delete this song?");
        builder.setTitle("Alert");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBaseService.deleteCurrentSong();
                updateMainPage();
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
     * update buttons
     */
    private void updateButtonDisplay () {
        if( ! mBaseService.isPlaying()){
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.mipmap.play_btn));
        }
        else {
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.mipmap.pause_btn));
        }

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
}
