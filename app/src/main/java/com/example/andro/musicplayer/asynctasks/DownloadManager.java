package com.example.andro.musicplayer.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.andro.musicplayer.DBManager;
import com.example.andro.musicplayer.fragments.SearchFrag;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

import static com.example.andro.musicplayer.MainActivity.pDialog;

public class DownloadManager extends AsyncTask<Void, Void, Void> {

    private String youtube_uri;
    private File video_folder;
    private File music_folder;
    private File general_playlist;
    private Context context;
    private boolean download_available;

    public DownloadManager(String youtube_uri, File video_folder, File music_folder, File general_playlist, Context context) {
        this.youtube_uri = youtube_uri;
        this.video_folder = video_folder;
        this.music_folder = music_folder;
        this.general_playlist = general_playlist;
        this.context = context;
        this.download_available = true;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int count;
        try {
            URL url = new URL(youtube_uri);
            URLConnection connection = url.openConnection();

            connection.connect();

            int lenghtOfFile = connection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            OutputStream output = new FileOutputStream(general_playlist.getAbsolutePath() + "/" + YoutubeManager.getSelected_song().replace(" ", "_") + ".wav");
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            download_available = false;
            pDialog.setProgressStyle(ProgressDialog.BUTTON_NEGATIVE);
            pDialog.setMessage("Ops... song not available for download.");
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setTitle("Processing song");
        pDialog.setMessage("Downloading...");
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        pDialog.setMessage("Converting...");
        final File wavSong = new File(general_playlist.getAbsolutePath() + "/" + YoutubeManager.getSelected_song().replace(" ", "_") + ".wav");
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                wavSong.delete();
                pDialog.setMessage("Inserting artist into database...");
                DBManager database = new DBManager(context);
                database.update_number_of_downloads_from_artist(SearchFrag.getArtist_selected());
                database.close();
                pDialog.dismiss();
            }

            @Override
            public void onFailure(Exception error) {
                File mp3Song = new File(general_playlist.getAbsolutePath() + "/" + YoutubeManager.getSelected_song().replace(" ", "_") + ".mp3");
                mp3Song.delete();
                wavSong.delete();
                pDialog.setProgressStyle(ProgressDialog.BUTTON_NEGATIVE);
                pDialog.setMessage("Couldn't convert to mp3. Deleting...");
                pDialog.dismiss();
            }
        };
        AndroidAudioConverter.with(context).setFile(wavSong).setFormat(AudioFormat.MP3).setCallback(callback).convert();
    }
}
