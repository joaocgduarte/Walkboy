package com.example.andro.musicplayer;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.andro.musicplayer.fragments.PlaylistFrag;
import com.example.andro.musicplayer.fragments.RecommendationsFrag;
import com.example.andro.musicplayer.fragments.SearchFrag;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    public static ProgressDialog pDialog;
    protected static File app_folder;
    protected static File video_folder;
    protected static File music_folder;
    protected static File general_playlist;
    protected static DBManager database;
    protected DrawerLayout drawer_layout;
    protected ActionBarDrawerToggle drawer_toggle;

    public static void setApp_folder(File app_folder) {
        MainActivity.app_folder = app_folder;
    }

    public static File getMusic_folder() {
        return music_folder;
    }

    public static void setMusic_folder(File music_folder) {
        MainActivity.music_folder = music_folder;
    }

    public static File getVideo_folder() {
        return video_folder;
    }

    public static void setVideo_folder(File video_folder) {
        MainActivity.video_folder = video_folder;
    }

    public static File getGeneral_playlist() {
        return general_playlist;
    }

    public static void setGeneral_playlist(File general_playlist) {
        MainActivity.general_playlist = general_playlist;
    }

    public static DBManager getDatabase() {
        return database;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        set_navigation_drawer_config();
        set_audio_converter();

        database = new DBManager(this);
    }

    public void set_audio_converter() {
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
            }

            @Override
            public void onFailure(Exception error) {
                // FFmpeg is not supported by device
            }
        });
    }

    public void set_navigation_drawer_config() {
        drawer_layout = findViewById(R.id.drawerLayout);
        drawer_toggle = new ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close);
        drawer_layout.addDrawerListener(drawer_toggle);
        drawer_toggle.syncState();

        NavigationView navigation_view = findViewById(R.id.navigation_view);
        if (navigation_view != null) {
            navigation_view.setNavigationItemSelectedListener(this);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawer_toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fr;
        if (item.getItemId() == R.id.nav_playlists) {
            fr = new PlaylistFrag();
        } else if (item.getItemId() == R.id.nav_search) {
            fr = new SearchFrag();
        } else if (item.getItemId() == R.id.nav_recomendations) {
            fr = new RecommendationsFrag();
        } else {
            System.exit(1);
            return true;
        }
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_fragment, fr);
        transaction.commit();
        drawer_layout.closeDrawers();

        SearchFrag.release_youtube_player();
        PlaylistFrag.release_media_player();
        return true;
    }
}
