package com.example.andro.musicplayer.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andro.musicplayer.MainActivity;
import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.adapters.AdapterRecommendationsSearch;
import com.example.andro.musicplayer.asynctasks.DownloadManager;
import com.example.andro.musicplayer.asynctasks.LastFMManager;
import com.example.andro.musicplayer.asynctasks.YoutubeManager;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;

import static com.example.andro.musicplayer.MainActivity.pDialog;

public class SearchFrag extends Fragment {

    protected static Context context;
    protected static AutoCompleteTextView search_text;
    protected static String song_selected, artist_selected;
    protected static TextView video_name;
    protected static YouTubePlayer player;
    protected static RecyclerView recyclerView;
    protected static List<String> artist_name_list;
    protected static List<String> image_artist_list;
    protected InputMethodManager imm;
    protected LinearLayoutManager layoutManager;
    protected Button search_on_youtube_button, download_button;
    protected List<String> songs = new ArrayList<String>();
    protected YouTubePlayerSupportFragment youtubePlayerFragment;
    protected AdapterRecommendationsSearch adapter_rec_view;

    public SearchFrag() {
        song_selected = "";
        artist_selected = "";
        artist_name_list = new ArrayList<String>();
        image_artist_list = new ArrayList<String>();
        context = getActivity();
    }

    public static void release_youtube_player() {
        if (getPlayer() != null) {
            getPlayer().release();
        }
    }

    public static String getSong_selected() {
        return song_selected;
    }

    public static AutoCompleteTextView getSearch_text() {
        return search_text;
    }

    public static List<String> getArtist_name_list() {
        return artist_name_list;
    }

    public static List<String> getImage_artist_list() {
        return image_artist_list;
    }

    public static RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public static String getArtist_selected() {
        return artist_selected;
    }

    public static YouTubePlayer getPlayer() {
        return player;
    }

    public static TextView getVideo_name() {
        return video_name;
    }

    public void initialize_variables() {
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        search_on_youtube_button = getView().findViewById(R.id.youtube_search);
        download_button = getView().findViewById(R.id.download_button);
        video_name = getView().findViewById(R.id.video_name);
        search_text = getView().findViewById(R.id.song_search);
        recyclerView = getView().findViewById(R.id.recycler_view);
        video_name.setSelected(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container != null) {
            container.removeAllViews();
        }
        context = getActivity();
        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        initialize_variables();
        initialize_youtube();
        set_recomendations_recycler();
        set_lastfm_to_search_text();
        set_search_button_onClick_listener();
        set_download_button_onClick_listener();
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void initialize_youtube() {
        youtubePlayerFragment = new YouTubePlayerSupportFragment();
        youtubePlayerFragment.initialize(YoutubeManager.getApiKey(), new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                if (!b) {
                    player = youTubePlayer;
                    player.setShowFullscreenButton(false);
                    player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(getActivity(), "fail", Toast.LENGTH_SHORT).show();
            }
        });
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_fragment, youtubePlayerFragment);
        fragmentTransaction.commit();
    }

    public void set_lastfm_to_search_text() {
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                return;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                return;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Timer timer = new Timer();
                if (search_text.getText().length() > 3) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            execute_last_fm(
                                    (MainActivity) getActivity(),
                                    false,
                                    false,
                                    true,
                                    false
                            );
                        }
                    }, 600);
                }
            }
        });
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void set_search_button_onClick_listener() {
        search_on_youtube_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                if (search_text.getText().length() > 0) {
                    empty_search_text();
                    execute_last_fm(
                            (MainActivity) getActivity(),
                            false,
                            false,
                            false,
                            true
                    );
                    update_recommendations_list();
                    YoutubeManager newVideo = new YoutubeManager();
                    newVideo.execute();

                }
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void set_download_button_onClick_listener() {
        download_button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.println(Log.ERROR, "DOWNLOAD: ", "Starting download..." + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                if ((!YoutubeManager.getId_video().isEmpty() || YoutubeManager.getId_video() != null) && !getSong_selected().isEmpty()) {
                    String youtubeLink = "http://youtube.com/watch?v=" + YoutubeManager.getId_video();
                    execute_download(youtubeLink);
                }

            }
        });
    }

    public void download_and_convert_video(String uri, File video_folder, File music_folder, File playlist_folder, Context context) {
        DownloadManager process_video = new DownloadManager(
                uri,
                video_folder,
                music_folder,
                playlist_folder,
                context
        );

        process_video.execute();

    }

    public void set_recomendations_recycler() {
        recyclerView.setLayoutManager(layoutManager);
        adapter_rec_view = new AdapterRecommendationsSearch(artist_name_list, image_artist_list, context);
        recyclerView.setAdapter(adapter_rec_view);
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.dropdown, songs);
        search_text.setThreshold(3);
        search_text.setAdapter(adapter);
    }

    public void update_recommendations_list() {
        recyclerView = getView().findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        adapter_rec_view = new AdapterRecommendationsSearch(artist_name_list, image_artist_list, context);
        recyclerView.setAdapter(adapter_rec_view);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void execute_last_fm(MainActivity main_activity,
                                boolean is_info, boolean is_recomendation,
                                boolean is_search, boolean is_youtube_rec) {
        LastFMManager last_fm = new LastFMManager(
                main_activity,
                is_info,
                is_recomendation,
                is_search,
                is_youtube_rec
        );
        last_fm.execute();
    }

    public void execute_download(String youtubeLink) {
        YouTubeUriExtractor ytEx = new YouTubeUriExtractor(getContext()) {

            public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                if (ytFiles != null) {
                    int itag = 22;
                    if (ytFiles.get(itag) == null) {
                        pDialog = new ProgressDialog(context);
                        pDialog.setTitle("Download not available");
                        pDialog.setMessage("Click outside this box to continue.");
                        pDialog.setCancelable(true);
                        pDialog.show();

                    } else {
                        String downloadUrl = ytFiles.get(itag).getUrl();
                        Log.println(Log.ERROR, "YoutubeExtractor::", downloadUrl);
                        download_and_convert_video(
                                downloadUrl,
                                MainActivity.getVideo_folder(),
                                MainActivity.getMusic_folder(),
                                MainActivity.getGeneral_playlist(),
                                getContext()
                        );
                    }
                }
            }
        };

        ytEx.execute(youtubeLink);
    }

    public void empty_search_text() {
        song_selected = search_text.getText().toString();
        search_text.setText("");
        artist_selected = song_selected.replaceAll("\\-.*", "").trim();
    }

}