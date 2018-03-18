package com.example.andro.musicplayer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.andro.musicplayer.MainActivity;
import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.adapters.AdapterSongs;
import com.example.andro.musicplayer.fragments.PlaylistFrag;

import java.io.File;

public class MoveSongDialog extends AppCompatDialogFragment {

    private final File[] available_playlists = MainActivity.getMusic_folder().listFiles();
    private File selected_song;
    private Spinner dropdown;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.move_song_dialog, null);
        final Spinner dropdown = view.findViewById(R.id.dropdown_available_playlists);

        final String[] available_playlists_names = new String[available_playlists.length];
        for (int i = 0; i < available_playlists.length; i++) {
            available_playlists_names[i] = available_playlists[i].getName();
            Log.println(Log.ERROR, "PRINT DROPDOWN::", available_playlists_names[i]);
        }

        ArrayAdapter adapter = new ArrayAdapter(PlaylistFrag.context, android.R.layout.simple_spinner_item, available_playlists_names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        builder.setView(view)
                .setTitle("Where do you want to move this song?")
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_song.renameTo(new File(available_playlists[dropdown.getSelectedItemPosition()].getAbsolutePath() + "/" + selected_song.getName()));
                        PlaylistFrag.getRecyclerView().setLayoutManager(PlaylistFrag.getLayoutManager());
                        RecyclerView.Adapter adapter = new AdapterSongs(PlaylistFrag.context, PlaylistFrag.getSelected_playlist().listFiles());
                        PlaylistFrag.getRecyclerView().setAdapter(adapter);
                        PlaylistFrag.getRecyclerView().getAdapter().notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    public void setSelected_song(File selected_song) {
        this.selected_song = selected_song;
    }
}
