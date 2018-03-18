package com.example.andro.musicplayer.asynctasks;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.andro.musicplayer.DBManager;
import com.example.andro.musicplayer.Helper;
import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.fragments.RecommendationsFrag;
import com.example.andro.musicplayer.fragments.SearchFrag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.example.andro.musicplayer.MainActivity.pDialog;

public class LastFMManager extends AsyncTask<Void, Void, Void> {

    private static final String API_KEY = "b69a431783f9b0e98cbe8787ff63e634";
    protected Context context;
    private boolean is_recommendation;
    private boolean is_search;
    private boolean is_get_song;
    private boolean is_youtube_rec;
    private String search;
    private String clicked_artist;
    private String sugested_song;
    private List<String> search_list;
    private List<String> artist_name_list;
    private List<String> image_artist_list;
    private String json_string;


    public LastFMManager(Context context, boolean is_get_song, boolean is_recommendation, boolean is_search, boolean is_youtube_rec) {
        this.context = context;
        this.is_get_song = is_get_song;
        this.is_recommendation = is_recommendation;
        this.is_search = is_search;
        this.is_youtube_rec = is_youtube_rec;
        if (SearchFrag.getSearch_text() != null) {
            search = SearchFrag.getSearch_text().getText().toString();
        }
        search_list = new ArrayList<String>();
        artist_name_list = new ArrayList<String>();
        image_artist_list = new ArrayList<String>();
        json_string = "";
        sugested_song = "";
        clicked_artist = "";

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (is_recommendation) {
            pDialog = new ProgressDialog(context);
            pDialog.setTitle("Reading your profile");
            pDialog.setMessage("Searching for artists that you might enjoy");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        } else if (is_get_song) {
            pDialog = new ProgressDialog(context);
            pDialog.setTitle("Searching for a song");
            pDialog.setMessage("Hopefully you'll like it!");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (is_search) {
            search_text_changer_list();
        } else if (is_recommendation) {
            search_recommendations();
        } else if (is_get_song) {
            Log.println(Log.ERROR, "TESTE::: ", "DOES DIS WORK???");
            sugested_song = recommend_random_song();
        } else if (is_youtube_rec) {
            youtube_recommend_artist_search();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (is_search) {
            update_search_adapters();
        } else if (is_recommendation) {
            update_recommendations();
            pDialog.dismiss();
        } else if (is_get_song) {
            pDialog.dismiss();
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Song", sugested_song);
            clipboard.setPrimaryClip(clip);
            Toast toast = Toast.makeText(context, "Copied to clipboard: " + sugested_song, Toast.LENGTH_SHORT);
            toast.show();
        } else if (is_youtube_rec) {
            update_youtube_recommendations();
        }

    }

    private void search_recommendations() {
        try {
            DBManager db = new DBManager(context);
            Cursor downloaded_data = db.get_all_data();

            if (downloaded_data.getCount() > 0) {
                while (downloaded_data.moveToNext()) {

                    String sUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getSimilar&artist=" + URLEncoder.encode(downloaded_data.getString(0), "UTF-8") + "&limit=10&api_key=" + API_KEY + "&format=json";
                    json_string = Helper.try_connection(sUrl);
                    JSONArray artists = get_json_array_artists();

                    int[] arr_indexes;
                    if (artists.length() < 5) {
                        arr_indexes = new int[artists.length()];
                    } else {
                        arr_indexes = new int[5];
                    }
                    Arrays.fill(arr_indexes, -1);

                    for (int j = 0; j < downloaded_data.getInt(1) && j < artists.length() && j < 5; j++) {
                        Random r = new Random();
                        int rand_index;
                        boolean is_valid;
                        do {
                            is_valid = true;
                            rand_index = r.nextInt(artists.length());
                            for (int i = 0; i < j; i++) {
                                if (arr_indexes[i] == rand_index) {
                                    is_valid = false;
                                    break;
                                }
                            }
                        } while (!is_valid);
                        arr_indexes[j] = rand_index;
                        select_artist(rand_index, artists);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void youtube_recommend_artist_search() {
        try {
            String sUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getSimilar&artist=" + URLEncoder.encode(SearchFrag.getArtist_selected(), "UTF-8") + "&limit=20&api_key=" + API_KEY + "&format=json";
            json_string = Helper.try_connection(sUrl);
            JSONArray artists = get_json_array_artists();

            for (int i = 0; i < artists.length() && i < 20; i++) {
                select_artist(i, artists);
            }

        } catch (Exception e) {
            Log.println(Log.ERROR, "LastFMManager:", "Erro no JSON - SearchFrag recommendation");
        }
    }

    private void update_recommendations() {
        RecommendationsFrag.getArtists_names().clear();
        RecommendationsFrag.getArtists_images().clear();
        RecommendationsFrag.getArtists_names().addAll(this.artist_name_list);
        RecommendationsFrag.getArtists_images().addAll(this.image_artist_list);
        RecommendationsFrag.getRecycler_view().getAdapter().notifyDataSetChanged();
        RecommendationsFrag.update_recommendations_list();
    }

    private void update_youtube_recommendations() {
        SearchFrag.getArtist_name_list().clear();
        SearchFrag.getImage_artist_list().clear();
        SearchFrag.getArtist_name_list().addAll(this.artist_name_list);
        SearchFrag.getImage_artist_list().addAll(this.image_artist_list);
        SearchFrag.getRecyclerView().getAdapter().notifyDataSetChanged();
    }

    private String recommend_random_song() {
        try {
            String sUrl = "http://ws.audioscrobbler.com/2.0/?method=track.search&track=%20&artist=" + URLEncoder.encode(clicked_artist, "UTF-8") + "&limit=10&api_key=" + API_KEY + "&format=json";
            json_string = Helper.try_connection(sUrl);
            JSONArray tracks = get_track_matches();
            Random r = new Random();
            int index = r.nextInt(tracks.length());
            JSONObject curr_song = tracks.getJSONObject(index);
            Log.println(Log.ERROR, "TESTE::: ", curr_song.get("artist") + " - " + curr_song.get("name"));
            return curr_song.get("artist") + " - " + curr_song.get("name");

        } catch (Exception e) {
            return "";
        }
    }

    private void search_text_changer_list() {
        if (search.length() > 3) {
            try {
                String sUrl = "http://ws.audioscrobbler.com/2.0/?method=track.search&track=" + URLEncoder.encode(search, "UTF-8") + "&limit=10&api_key=" + API_KEY + "&format=json";
                json_string = Helper.try_connection(sUrl);

                JSONArray tracks = get_track_matches();

                for (int i = 0; i < tracks.length(); i++) {
                    JSONObject curr_song = tracks.getJSONObject(i);
                    search_list.add(curr_song.get("artist") + " - " + curr_song.get("name"));
                }

            } catch (Exception e) {
                Log.println(Log.ERROR, "LastFMManager:", "Erro no JSON - pesquisa");
            }
        }
    }

    private JSONArray get_json_array_artists() {
        try {
            JSONObject json_array = new JSONObject(json_string);
            JSONObject res = (JSONObject) json_array.get("similarartists");
            return (JSONArray) res.get("artist");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONArray get_track_matches() {
        try {
            JSONObject json_array = new JSONObject(json_string);
            JSONObject res = (JSONObject) json_array.get("results");
            JSONObject track_matches = (JSONObject) res.get("trackmatches");
            return (JSONArray) track_matches.get("track");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void select_artist(int i, JSONArray artists) {
        try {
            JSONObject artist = artists.getJSONObject(i);
            artist_name_list.add(artist.getString("name"));
            JSONArray this_artist_images = (JSONArray) artist.get("image");
            JSONObject this_artist_selected_image = this_artist_images.getJSONObject(2);
            image_artist_list.add(this_artist_selected_image.getString("#text"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update_search_adapters() {
        SearchFrag.getSearch_text().setAdapter(null);
        ArrayAdapter adapter = new ArrayAdapter(context, R.layout.dropdown, search_list);
        SearchFrag.getSearch_text().setThreshold(3);
        SearchFrag.getSearch_text().setAdapter(adapter);
    }

    public void setClicked_artist(String clicked_artist) {
        this.clicked_artist = clicked_artist;
    }
}
