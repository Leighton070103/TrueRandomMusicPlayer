package net.classicgarage.truerandommusicplayer.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.widget.TabHost;

import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.activity.tabactivity.AllMusicTabActivity;
import net.classicgarage.truerandommusicplayer.activity.tabactivity.FavoriteTabActivity;

/**
 * Created by tomat on 2017-05-16.
 */

public class MenuTabActivity extends TabActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {

        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_song_list);

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        TabHost.TabSpec allMusicTap = tabHost.newTabSpec("AllMusic");
        TabHost.TabSpec favoriteTap = tabHost.newTabSpec("Favorite");

        allMusicTap.setIndicator("ALLMUSIC");
        allMusicTap.setContent(new Intent(this, AllMusicTabActivity.class));
        favoriteTap.setIndicator("FAVORITE");
        favoriteTap.setContent(new Intent(this, FavoriteTabActivity.class));

        tabHost.addTab(allMusicTap);
        tabHost.addTab(favoriteTap);
    }
}
