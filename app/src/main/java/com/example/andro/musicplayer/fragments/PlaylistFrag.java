package com.example.andro.musicplayer.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.andro.musicplayer.Helper;
import com.example.andro.musicplayer.MainActivity;
import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.adapters.AdapterPlaylists;
import com.example.andro.musicplayer.adapters.AdapterSongs;
import com.example.andro.musicplayer.dialogs.NewPlaylistDialog;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class PlaylistFrag extends Fragment {

    public static Context context;
    protected static FragmentManager fragment_manager;
    protected static LinearLayoutManager layoutManager;
    protected static RecyclerView recyclerView;
    protected static ImageButton play_button;
    protected static TextView current_time, full_time, song_name, folder_name;
    protected static SeekBar seekbar;
    protected static int index_song;
    protected static File selected_playlist;
    protected static File[] selected_playlist_songs;
    protected static boolean is_shuffle, is_repeat, is_playing;
    protected static MediaPlayer media_player;
    protected static Handler handler;
    protected static Resources res;
    protected ImageButton return_button, prev_song_button, add_playlist_button, repeat_button, next_song_button, shuffle_button;
    private Runnable update_time = new Runnable() {
        public void run() {
            int current_duration;
            int to_finish_duration;
            int full_duration;
            if (media_player != null) {
                if (media_player.isPlaying()) {
                    current_duration = media_player.getCurrentPosition();
                    full_duration = media_player.getDuration();
                    to_finish_duration = (full_duration - current_duration);
                    update_player_timer(current_duration, to_finish_duration);
                    current_time.postDelayed(this, 1000);
                } else {
                    current_time.removeCallbacks(this);
                }
            }
        }
    };

    public PlaylistFrag() {
        index_song = -1;
        selected_playlist = null;
        is_repeat = false;
        is_shuffle = false;
        is_playing = false;
    }

    public static void start_playing_songs() {
        if (selected_playlist != null) {
            release_media_player();
            media_player = new MediaPlayer();
            Uri u = Uri.parse(selected_playlist_songs[index_song].getAbsolutePath());
            Log.println(Log.ERROR, "Teste::", "Index: " + u.toString());
            try {
                media_player.setDataSource(u.toString());
                set_media_player_listeners();
                media_player.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected static void set_media_player_listeners() {
        media_player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekbar.setEnabled(true);
                play_button.performClick();
                song_name.setText(selected_playlist_songs[index_song]
                        .getName().replace("_", " ")
                        .replace(".mp3", ""));
                media_player.start();
                media_player.setVolume(1.0f, 1.0f);
                seekbar.setMax(media_player.getDuration());

                seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (media_player != null) {
                            if (media_player.isPlaying() && fromUser) {
                                media_player.seekTo(seekBar.getProgress());
                            }
                        }
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
                start_play_progress_updater();
            }
        });
        media_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                play_button.performClick();
                if (selected_playlist != null) {
                    index_song = generate_index(true);
                    update_adapter_to_selected_song();
                    start_playing_songs();
                } else {
                    release_media_player();
                    seekbar.setProgress(00);
                    song_name.setText("No song selected");
                    current_time.setText("0:00");
                    full_time.setText("0:00");
                }
                seekbar.setEnabled(false);
            }
        });
    }

    public static void release_media_player() {
        if (media_player != null) {
            media_player.release();
            media_player = null;
        }
    }

    public static int generate_index(boolean is_next) {
        if (!is_shuffle) {
            if (!is_repeat) {
                if (is_next) {
                    index_song += 1;
                } else {
                    index_song -= 1;
                }
            }
        } else {
            Random r = new Random();
            index_song = r.nextInt(selected_playlist_songs.length);
        }
        if (index_song >= selected_playlist_songs.length) {
            index_song = 0;
        }
        if (index_song < 0) {
            index_song = 0;
        }
        return index_song;
    }

    public static void initiate_recycler(File[] files, boolean is_playlist) {
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.Adapter adapter;
        if (is_playlist) {
            adapter = new AdapterPlaylists(context, files);
        } else {
            Arrays.sort(files);
            adapter = new AdapterSongs(context, files);
        }
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public static void update_recycler_to_songs(File playlist) {
        setSelected_playlist(playlist);
        selected_playlist_songs = playlist.listFiles();
        Arrays.sort(selected_playlist_songs);
        setSelected_playlist_songs(selected_playlist_songs);
        initiate_recycler(selected_playlist_songs, false);
        folder_name.setText(selected_playlist.getName());
    }

    public static void start_play_progress_updater() {
        if (media_player != null) {
            if (media_player.isPlaying()) {
                seekbar.setProgress(media_player.getCurrentPosition());
                Runnable notification = new Runnable() {
                    public void run() {
                        start_play_progress_updater();
                    }
                };
                handler.postDelayed(notification, 1000);
            }
        }
    }

    public static void update_adapter_to_selected_song() {
        AdapterSongs adp = (AdapterSongs) recyclerView.getAdapter();
        AdapterSongs.setSelectedPos(index_song);
        adp.notifyDataSetChanged();
    }

    public static void setIndex_song(int index_song) {
        PlaylistFrag.index_song = index_song;
    }

    public static void setSelected_playlist_songs(File[] selected_playlist_songs) {
        PlaylistFrag.selected_playlist_songs = selected_playlist_songs;
    }

    public static RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public static LinearLayoutManager getLayoutManager() {
        return layoutManager;
    }

    public static FragmentManager getFragment_manager() {
        return fragment_manager;
    }

    public static MediaPlayer getMusic_player() {
        return media_player;
    }

    public static File getSelected_playlist() {
        return selected_playlist;
    }

    public static void setSelected_playlist(File selected_playlist2) {
        selected_playlist = selected_playlist2;
    }

    protected void initiate_variables() {
        recyclerView = getView().findViewById(R.id.recycler_view_playlists);
        return_button = getView().findViewById(R.id.return_button);
        add_playlist_button = getView().findViewById(R.id.add_playlist_button);
        repeat_button = getView().findViewById(R.id.repeat_button);
        prev_song_button = getView().findViewById(R.id.prev_song_button);
        play_button = getView().findViewById(R.id.play_button);
        next_song_button = getView().findViewById(R.id.next_song_button);
        shuffle_button = getView().findViewById(R.id.shuffle_button);
        current_time = getView().findViewById(R.id.current_time);
        full_time = getView().findViewById(R.id.full_time);
        song_name = getView().findViewById(R.id.song_name);
        song_name.setSelected(true);
        folder_name = getView().findViewById(R.id.folder_name);
        seekbar = getView().findViewById(R.id.seekbar);
        seekbar.setEnabled(false);
        handler = new Handler();
        folder_name.setSelected(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        PlaylistFrag.context = getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release_media_player();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        release_media_player();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        layoutManager = new LinearLayoutManager(context);
        fragment_manager = getFragmentManager();
        create_folders();
        View view = inflater.inflate(R.layout.playlist_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        res = getResources();
        initiate_variables();
        initiate_recycler(MainActivity.getMusic_folder().listFiles(), true);
        set_return_button_onClick_listener();
        set_shuffle_button_onClick_listener();
        set_repeat_button_onClick_listener();
        set_add_playlist_button_onClick_listener();
        set_play_button_onClick_listener();
        set_next_song_button_onClick_listener();
        set_prev_song_button_onClick_listener();
    }

    private void update_player_timer(int currentDuration, int to_finish_duration) {
        current_time.setText("" + Helper.milliseconds_to_timer((long) currentDuration));
        full_time.setText("" + Helper.milliseconds_to_timer((long) to_finish_duration));
    }

    public void set_next_song_button_onClick_listener() {
        next_song_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (media_player != null) {
                    if (selected_playlist != null) {
                        media_player.stop();
                        index_song = generate_index(true);
                        update_adapter_to_selected_song();
                        start_playing_songs();
                    }
                }
            }
        });
    }

    public void set_prev_song_button_onClick_listener() {
        prev_song_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (media_player != null) {
                    if (selected_playlist != null) {
                        media_player.stop();
                        index_song = generate_index(false);
                        update_adapter_to_selected_song();
                        start_playing_songs();
                    }
                }
            }
        });
    }

    public void set_return_button_onClick_listener() {
        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected_playlist != null) {
                    setSelected_playlist(null);
                    setSelected_playlist_songs(null);
                    AdapterSongs adp = (AdapterSongs) recyclerView.getAdapter();
                    AdapterSongs.setSelectedPos(RecyclerView.NO_POSITION);
                    initiate_recycler(MainActivity.getMusic_folder().listFiles(), true);
                    folder_name.setText("Playlists");
                }
            }
        });
    }

    public void set_play_button_onClick_listener() {
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (media_player != null) {
                    if (media_player.isPlaying()) {
                        seekbar.setEnabled(false);
                        media_player.pause();
                        play_button.setImageResource(R.mipmap.ic_play);
                    } else {
                        seekbar.setEnabled(true);
                        media_player.start();
                        play_button.setImageResource(R.mipmap.ic_pause);
                        current_time.post(update_time);
                    }
                }
            }
        });
    }

    public void set_shuffle_button_onClick_listener() {
        shuffle_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_shuffle) {
                    shuffle_button.setImageResource(R.mipmap.ic_shuffle);
                    is_shuffle = false;
                } else {
                    shuffle_button.setImageResource(R.mipmap.ic_shuffle_green);
                    is_shuffle = true;
                    if (is_repeat) {
                        repeat_button.setImageResource(R.mipmap.ic_repeat);
                        is_repeat = false;
                    }
                }
            }
        });
    }

    public void set_add_playlist_button_onClick_listener() {
        add_playlist_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewPlaylistDialog playlistDialog = new NewPlaylistDialog();
                playlistDialog.show(fragment_manager, "new playlist dialog");
            }
        });
    }

    public void set_repeat_button_onClick_listener() {
        repeat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_repeat) {
                    repeat_button.setImageResource(R.mipmap.ic_repeat);
                    is_repeat = false;
                } else {
                    repeat_button.setImageResource(R.mipmap.ic_repeat_green);
                    is_repeat = true;
                    if (is_shuffle) {
                        shuffle_button.setImageResource(R.mipmap.ic_shuffle);
                        is_shuffle = false;
                    }
                }
            }
        });
    }

    public void create_folders() {
        MainActivity.setApp_folder(
                create_storage_on_card("/sdcard/SongDrawer")
        );
        MainActivity.setMusic_folder(
                create_storage_on_card("/sdcard/SongDrawer/Music")
        );
        MainActivity.setGeneral_playlist(
                create_storage_on_card("/sdcard/SongDrawer/Music/General")
        );
    }

    public File create_storage_on_card(String folder) {
        File direct = new File(folder);
        if (!direct.exists()) {
            if (direct.mkdir()) ; //directory is created;
            Log.println(Log.ERROR, "SearchFrag:", folder + " CREATED in: " + direct.getAbsolutePath());
        } else {
            Log.println(Log.ERROR, "SearchFrag:", folder + " ALREADY EXISTS in: " + direct.getAbsolutePath());
        }
        return direct;
    }
}
