package net.classicgarage.truerandommusicplayer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;

import net.classicgarage.truerandommusicplayer.BuildConfig;
import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.adapter.SwipePagerAdapter;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;
import net.classicgarage.truerandommusicplayer.service.BaseService;
import net.classicgarage.truerandommusicplayer.service.MusicService;
import net.classicgarage.truerandommusicplayer.util.GlideCircleTransform;
import net.classicgarage.truerandommusicplayer.util.SwipeViewPager;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static net.classicgarage.truerandommusicplayer.activity.SongListActivity.SONG_POSITION;
import static net.classicgarage.truerandommusicplayer.fragment.FavSongFragment.PLAY_MODE;
import static net.classicgarage.truerandommusicplayer.service.MusicService.FAV_MODE;
import static net.classicgarage.truerandommusicplayer.service.MusicService.FAV_RANDOM;
import static net.classicgarage.truerandommusicplayer.service.MusicService.FAV_SEQUENCE;
import static net.classicgarage.truerandommusicplayer.service.MusicService.NORMAL_MODE;
import static net.classicgarage.truerandommusicplayer.service.MusicService.NORMAL_RANDOM;
import static net.classicgarage.truerandommusicplayer.service.MusicService.NORMAL_SEQUENCE;
import static net.classicgarage.truerandommusicplayer.service.MusicService.REPLAY_FLAG;

/**
 * This activity is to provide the main view for this app.
 * And enable users to check the song lists, play, pause, delete music, or label them as favorite.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 070103;

    ImageButton mPlayPauseBtn;
    ImageView mAlbumArtView;
    ImageButton mRandomBtn;
    ImageButton mReplayBtn;
    ImageButton mNextBtn;
    ImageButton mPreBtn;
    ToggleButton mFavoriteBtn;
    ImageButton mPlayListBtn;
    ImageButton mDeleteBtn;
    SearchView mSearchView;
    ListView mSongListLv;
    static SeekBar sSeekBar;
    TextView mSongTitleTv;
    TextView mAuthorTv;
    TextView mAlbumTv;
    TextView mSongTimeTv;
    static TextView mSongLeftTimeTv;
    ImageView mNoMusicImg;

    private ServiceConnection mServiceCon;
    private BaseService mBaseService;
    private SongDataSource mSongDataSource;

    private SwipeViewPager mViewPager;
    private List<View> mAlbumImageViewList = new ArrayList<View>();
    private SwipePagerAdapter mSwipePagerAdapter;
    private ArrayAdapter mArrayAdapter;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private Toast mToast;
    private int currentIndex;

    public static Handler musicInfoHandler = new Handler(Looper.getMainLooper()){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MusicService.REFRESH_ALBUM_VIEW: {
                    //enableSwiping();
                    break;
                }
                case MusicService.REFRESH_SEEK_BAR_: {
                    Bundle bundle = msg.getData();
                    int duration = bundle.getInt("duration");
                    int position = bundle.getInt("position");
                    try {
                        sSeekBar.setMax(duration);
                        sSeekBar.setProgress(position);
                        mSongLeftTimeTv.setText(SongItem.formateTime(position) + "");
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    };

    //public static Handler

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
        mSearchView = (SearchView)  findViewById(R.id.search_sv);
        mSongListLv = (ListView) findViewById(R.id.song_list_lv);
        mNoMusicImg = (ImageView) findViewById(R.id.no_music_image_view);

        mPlayPauseBtn.setOnClickListener(this);
        mPreBtn.setOnClickListener(this);
        mPlayListBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);
        mFavoriteBtn.setOnClickListener(this);
        mRandomBtn.setOnClickListener(this);
        mReplayBtn.setOnClickListener(this);

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) mSongListLv.setVisibility(VISIBLE);
                else mSongListLv.setVisibility(GONE);
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    // Clear the text filter.
                    mSongListLv.clearTextFilter();
                } else {
                    // Sets the initial value for the text filter.
                    mSongListLv.setFilterText(newText.toString());
                }
                return false;
            }
        });


//        AudioManager audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
//        ComponentName name = new ComponentName(this.getPackageName(),
//                MediaButtonReceiver.class.getName());
//        audioManager.registerMediaButtonEventReceiver(name);
        getPermissions();

    }

    public void debug(){
        if(BuildConfig.DEBUG){
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll().build());
        }
    }

    /**
     * This method is check permissions which is necessary for this app, and if the permission is
     * not granted, request it from the user.
     */
    private void getPermissions() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                == PackageManager.PERMISSION_DENIED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE )
                == PackageManager.PERMISSION_DENIED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE )
                == PackageManager.PERMISSION_DENIED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SYSTEM_ALERT_WINDOW )
                        == PackageManager.PERMISSION_DENIED )
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW
            }, 1);
        }
        else{
            loadApplication();
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
                loadApplication();
        }
    }

    public void loadApplication(){
        configServices();
        setSeekBarListener();
        initSearchView();
        enableSwiping();
    }

    /**
     * To initialize the search view.
     */
    public void initSearchView(){

        mArrayAdapter = new ArrayAdapter<SongItem>(getApplicationContext(),
                android.R.layout.simple_expandable_list_item_1, mSongDataSource.getAllSongs());
        mSongListLv.setAdapter(mArrayAdapter);

        mSongListLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBaseService.callPlaySongAtPosition(position);
                mBaseService.callContinueMusic();
                updateButtonDisplay();
                mSongListLv.setVisibility(GONE);
            }
        });
        mSongListLv.setTextFilterEnabled(true);
    }

    /**
     * This method is to start the music service.
     */
    private void configServices(){
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);
        //Message msg = Message.obtain(null,MusicService.REQUESTING_BINDING,0,0);
        mServiceCon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBaseService = (BaseService) service;
                updateMainPage();
                SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                Integer playMode = preferences.getInt( MusicService.PLAY_MODE, NORMAL_SEQUENCE );
                boolean replayFlag = preferences.getBoolean(REPLAY_FLAG, false);
                mBaseService.setPlayMode(playMode);
                mBaseService.callChangeReplayFlag(replayFlag);
                initializeModeBtn(playMode, replayFlag);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        getApplicationContext().bindService(intent, mServiceCon, BIND_AUTO_CREATE);
    }

    private void initializeModeBtn(Integer playMode, boolean replayFlag){
        switch (playMode){
            case NORMAL_SEQUENCE:
                mRandomBtn.setImageDrawable(getResources().getDrawable(R.drawable.non_random_playlist));
                break;
            case FAV_SEQUENCE:
                mRandomBtn.setImageDrawable(getResources().getDrawable(R.drawable.non_random_favorite));
                break;
            case NORMAL_RANDOM:
                mRandomBtn.setImageDrawable(getResources().getDrawable(R.drawable.random_playlist));
                break;
            case FAV_RANDOM:
                mRandomBtn.setImageDrawable(getResources().getDrawable(R.drawable.random_favorite));
                break;
        }
        if(replayFlag) mReplayBtn.setImageDrawable(getResources().getDrawable(R.mipmap.replay1w));
        else mReplayBtn.setImageDrawable(getResources().getDrawable( R.mipmap.replayb));
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
     *
     * @param positionInList
     * @param isFistInitialized
     */
    public void initListViews(int positionInList, boolean isFistInitialized){
        if(positionInList<mSongDataSource.getAllSongs().size()){
            //Log.d("pos to be added:", String.valueOf(positionInList));
            LayoutInflater inflater = getLayoutInflater();
            View album_view = inflater.inflate(R.layout.album_img_layout,null);
            mAlbumArtView = (ImageView) album_view.findViewById(R.id.albumView);
            Glide.with(this).load(mSongDataSource.getAllSongs().get(positionInList).getCoverUri())
                    .transform( new GlideCircleTransform(this)).into(mAlbumArtView);
            mAlbumImageViewList.add(album_view);
            //Log.d("albumImageViewList:", String.valueOf(mAlbumImageViewList.size()));
            if(isFistInitialized){
                initListViews(positionInList+1,false);
            }
        }
    }

    /**
     * Initialization of the swipe view.
     * @throws IllegalAccessException
     */
    public void initSwipeView() throws IllegalAccessException{
        /*mAlbumImageViewList = new ArrayList<View>();
        if( mBaseService == null){
            mViewPager.setVisibility(GONE);
            mNoMusicImg.setVisibility(VISIBLE);
        }
        final int currentItem = Integer.MAX_VALUE / 2;
        mViewPager.setCurrentItem(currentItem);*/

        //mViewPager = (ViewPager) findViewById(R.id.swipe_viewpager);
        LayoutInflater inflater = getLayoutInflater();
        View album_view = inflater.inflate(R.layout.album_img_layout,null);
        mAlbumArtView = (ImageView) album_view.findViewById(R.id.albumView);
        initListViews(0,true);
        mViewPager = (SwipeViewPager) findViewById(R.id.swipe_viewpager);
        mSwipePagerAdapter = new SwipePagerAdapter(mAlbumImageViewList);
        mViewPager.setAdapter(mSwipePagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
            /**
             * position:current page;
             * positionOffset: offset of current page
             * positionOffsetPixels: offset pixels of current page
             */
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d("position", String.valueOf(position));
                updateMainPage();
            }
            @Override
            public void onPageSelected(int position) {
                // play song at the position when swipe to another page
                mBaseService.callPlaySongAtPosition(position);
                mBaseService.callSeekTo(0);
                updateMainPage();
                if (position == mViewPager.getAdapter().getCount() - 1) {
                    initListViews((mBaseService.getPlayingSongIndex()+1),false);// listViews添加数据 // 滑动到最后一页
 //                   mSwipePagerAdapter.setListViews(mAlbumImageViewList);// 重构adapter对象  这是一个很重要
                    mSwipePagerAdapter.notifyDataSetChanged();// 刷新
                    Log.d("song index: ", String.valueOf(mBaseService.getPlayingSongIndex()));
                }
            }
            /**
             * state == ViewPager.SCROLL_STATE_DRAGGING ||  ViewPager.SCROLL_STATE_SETTLING
             * || ViewPager.SCROLL_STATE_IDLE
             */
            @Override
            public void onPageScrollStateChanged(int state) {}
        };
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mViewPager.getParent().requestDisallowInterceptTouchEvent(true);
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
     * The onclick method for all the buttons.
     * @param v
     */
    public void onClick(View v) {
        try{
            switch (v.getId()){
                case R.id.play_pause_btn:
                    mSongTitleTv.setText(mBaseService.getPlayingSong().getTitle());
                    mSongTimeTv.setText(mBaseService.getPlayingSong().getSongTime());
                    if(mBaseService.isPlaying()){
                        mBaseService.callPause();
                    }
                    else if(sSeekBar.getProgress() == 0) mBaseService.callPlay();
                    else{
                        mBaseService.callContinueMusic();
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
                    Log.d("the current position", String.valueOf(mBaseService.getPlayingSongIndex()+1));
                    initListViews(mBaseService.getPlayingSongIndex(),false);// listViews添加数据
                    mSwipePagerAdapter.setListViews(mAlbumImageViewList);// 重构adapter对象  这是一个很重要
                    mSwipePagerAdapter.notifyDataSetChanged();// 刷新
                    mViewPager.setCurrentItem(mBaseService.getPlayingSongIndex(),false);
                    currentIndex = mBaseService.getPlayingSongIndex();
                    updateMainPage();
                    break;
                case R.id.pre_btn:
                    //mBaseService.callPause();
                    mBaseService.callPlayLastSong();
                    Log.d("the current position", String.valueOf(mBaseService.getPlayingSongIndex()+1));
                    initListViews(mBaseService.getPlayingSongIndex(),false);// listViews添加数据
                    mSwipePagerAdapter.setListViews(mAlbumImageViewList);// 重构adapter对象  这是一个很重要
                    mSwipePagerAdapter.notifyDataSetChanged();// 刷新
                    mViewPager.setCurrentItem(mBaseService.getPlayingSongIndex(), false);
                    currentIndex = mBaseService.getPlayingSongIndex();
                    updateMainPage();
                    break;
                case R.id.activity_main_delete_btn:
                    deleteDialog();
                    break;
                case R.id.favorite_btn:
                    mBaseService.setCurrentSongFavorite();
                    break;
                case R.id.random_btn:
                    updateModeButton();
                    break;
                case R.id.replay_btn:
                    updateReplayButton();
                    break;
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    /**
     * To display a delete dialog.
     */
    protected void deleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.delete_alert));
        builder.setTitle(getString(R.string.alert));
        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBaseService.deleteCurrentSong();
                MediaScannerConnection.scanFile(getApplicationContext(),new String[] {Environment.getExternalStorageDirectory().getAbsolutePath()}, null, null);
                updateMainPage();
                mArrayAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
            if( song != null) {
                mSongTitleTv.setText( song.getTitle());
                mAuthorTv.setText( song.getArtist());
                mAlbumTv.setText( song.getAlbum() );
                mSongTimeTv.setText(mBaseService.getPlayingSong().getSongTime());
                mFavoriteBtn.setChecked(song.getFavorite());
            }
            else {
                mSongTitleTv.setText("No Music");
                mViewPager.setVisibility(GONE);
                mNoMusicImg.setVisibility(VISIBLE);
            }
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

    private void updateModeButton()
    {
        String s = "";
        switch (mBaseService.getPlayMode()){
            case NORMAL_SEQUENCE:
                mBaseService.setPlayMode(NORMAL_RANDOM);
                mRandomBtn.setImageDrawable(getResources().getDrawable(R.drawable.random_playlist));
                s = "Sequence play";
                break;
            case NORMAL_RANDOM:
                mBaseService.setPlayMode(FAV_MODE);
                mRandomBtn.setImageDrawable(getResources().getDrawable(R.drawable.non_random_favorite));
                s = "Sequence favorite";
                break;
            case FAV_MODE:
                mBaseService.setPlayMode(FAV_RANDOM);
                mRandomBtn.setImageDrawable(getResources().getDrawable(R.drawable.random_favorite));
                s = "Random favorite";
                break;
            case FAV_RANDOM:
                mBaseService.setPlayMode(NORMAL_SEQUENCE);
                mRandomBtn.setImageDrawable(getResources().getDrawable(R.drawable.non_random_playlist));
                s = "Random play";
                break;
        }
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getSharedPreferences("user",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt( PLAY_MODE, mBaseService.getPlayMode() );
        editor.commit();
    }

    private void updateReplayButton() {

        if( mBaseService.callGetReplayFlag()) {
            mReplayBtn.setImageDrawable(getResources().getDrawable(R.mipmap.replayb));
            mBaseService.callChangeReplayFlag();

        }
        else {
            mReplayBtn.setImageDrawable(getResources().getDrawable(R.mipmap.replay1w));
            mBaseService.callChangeReplayFlag();
            Toast.makeText(getApplicationContext(), "Repeat", Toast.LENGTH_SHORT).show();
        }
        SharedPreferences preferences = getSharedPreferences("user",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean( REPLAY_FLAG, mBaseService.callGetReplayFlag() );
        editor.commit();
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

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            Bundle extras = intent.getExtras();
            int position = extras.getInt(SONG_POSITION);
            int mode = extras.getInt(PLAY_MODE);
            if( mode == FAV_MODE) mBaseService.setPlayMode( FAV_MODE, null);
            else mBaseService.setPlayMode( NORMAL_MODE, null);
            mBaseService.callPlaySongAtPosition(position);
            sSeekBar.setProgress(0);
            initializeModeBtn(mBaseService.getPlayMode(), mBaseService.callGetReplayFlag());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(mServiceCon);
    }
}
