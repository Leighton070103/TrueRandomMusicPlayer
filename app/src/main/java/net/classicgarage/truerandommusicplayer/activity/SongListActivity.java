package net.classicgarage.truerandommusicplayer.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.TabHost;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.fragment.AllSongFragment;
import net.classicgarage.truerandommusicplayer.fragment.FavSongFragment;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import static android.R.id.tabhost;

/**
 * This activity is to display two sub activities of two songs.
 */
public class SongListActivity extends FragmentActivity {

    private ArrayAdapter<SongItem> mSearchSongsAdapter;
    private ListView mSongsLv;
    private FragmentTabHost mTabHost;
    private ImageButton mReturnBtn;
    private SearchView mSearchView;
    public static final String SONG_POSITION = "songPosition";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        setContentView(R.layout.activity_song_list);
        mSearchSongsAdapter = new ArrayAdapter<SongItem>(getApplicationContext(),
                android.R.layout.simple_expandable_list_item_1, SongDataSource.getInstance(
                getApplicationContext()).getAllSongs());
        mSearchView = (SearchView) findViewById(R.id.songlist_searchbar_sv);
        mSongsLv = (ListView) findViewById(R.id.song_list_lv);
        mSongsLv.setAdapter(mSearchSongsAdapter);


        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) mSongsLv.setVisibility(View.VISIBLE);
                else mSongsLv.setVisibility(View.GONE);
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
                    mSongsLv.clearTextFilter();
                } else {
                    // Sets the initial value for the text filter.
                    mSongsLv.setFilterText(newText.toString());
                }
                return false;
            }
        });

        mSongsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent result = new Intent();
                result.putExtra(SONG_POSITION, position);
                setResult(RESULT_OK, result);
                finish();
            }
        });

        mSongsLv.setTextFilterEnabled(true);

        super.onCreate(savedInstanceState);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongListActivity.this.finish();
            }
        });

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("All").setIndicator("All"), AllSongFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("Favorite").setIndicator("Favorite"), FavSongFragment.class, null);
//        mTabHost.setup();
//        TabHost.TabSpec allMusicTab = mTabHost.newTabSpec("All music");
//        TabHost.TabSpec favoriteTab = mTabHost.newTabSpec("Favorite");
//
//        allMusicTab.setIndicator("AllMusic");
//        allMusicTab.setContent(new Intent(this, AllSongsActivity.class));
//        favoriteTab.setIndicator("Favorite");
//        favoriteTab.setContent(new Intent(this, FavSongsActivity.class));
//
//        mTabHost.addTab(allMusicTab);
//        mTabHost.addTab(favoriteTab);

    }
}
