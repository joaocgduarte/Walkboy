package com.example.andro.musicplayer.asynctasks;


import android.os.AsyncTask;
import android.util.Log;

import com.example.andro.musicplayer.Helper;
import com.example.andro.musicplayer.fragments.SearchFrag;
import com.google.android.youtube.player.YouTubePlayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class YoutubeManager extends AsyncTask<Void, Void, Void> {

    protected static final String API_KEY = "AIzaSyDVZuVtS950ysNniEYdJs-OyBclB6QwxJU";
    protected static String selected_song;
    protected static String id_video;
    protected String json_string;
    protected String title_video;

    public YoutubeManager() {
        selected_song = SearchFrag.getSong_selected();
        json_string = "";
        id_video = "";
        title_video = "";
    }

    public static String getApiKey() {
        return API_KEY;
    }

    public static String getId_video() {
        return id_video;
    }

    public static String getSelected_song() {
        return selected_song;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            String sUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + URLEncoder.encode(selected_song, "UTF-8") + "&type=video&key=" + API_KEY;
            json_string = Helper.try_connection(sUrl);
            JSONObject json_array = new JSONObject(json_string);
            JSONArray videos = (JSONArray) json_array.get("items");
            JSONObject selected_video = (JSONObject) videos.get(0);
            JSONObject video_id = (JSONObject) selected_video.get("id");
            JSONObject video_snippet = (JSONObject) selected_video.get("snippet");
            id_video = video_id.getString("videoId");        //IMPORTANT
            title_video = video_snippet.getString("title");
        } catch (Exception e) {
            Log.println(Log.ERROR, "YoutubeManager:", "Erro no Json");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        SearchFrag.getVideo_name().setText(title_video);
        SearchFrag.getPlayer().loadVideo(id_video);
        SearchFrag.getPlayer().setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
    }
}
