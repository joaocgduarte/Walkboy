package com.example.andro.musicplayer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Helper {

    public static String milliseconds_to_timer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            finalTimerString = hours + ":";
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        return finalTimerString;
    }

    public static String try_connection(String sUrl) {
        try {
            URL url = new URL(sUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            InputStream input_stream = con.getInputStream();
            BufferedReader buffered_reader = new BufferedReader(new InputStreamReader(input_stream));
            String json_string = "";
            String line = "";
            while (line != null) {
                line = buffered_reader.readLine();
                json_string += line;
            }
            return json_string;
        } catch (Exception e) {
            Log.println(Log.ERROR, "Getting json:", "Http Connection failed: " + sUrl);
            return "";
        }
    }

    public static String format_name(String file_name) {
        return file_name.replace(" ", "_")
                .replace(".", "")
                .replace(".mp3", "")
                .replace("/", "").trim();
    }

    public static String deformat_name(String file_name) {
        return file_name.replace("_", " ").replace(".mp3", "");
    }
}
