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
import android.widget.EditText;

import com.example.andro.musicplayer.Helper;
import com.example.andro.musicplayer.MainActivity;
import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.adapters.AdapterPlaylists;
import com.example.andro.musicplayer.adapters.AdapterSongs;
import com.example.andro.musicplayer.fragments.PlaylistFrag;

import java.io.File;


public class EditFileDialog extends AppCompatDialogFragment {

    private EditText new_name;
    private File old_playlist;
    private String file_type;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_playlist_dialog, null);

        builder.setView(view)
                .setTitle("Edit " + old_playlist.getName().replace("_", " ").replace(".mp3", ""))
                .setMessage("NOTE: '.' and '/' are invalid characters!")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!new_name.getText().toString().isEmpty()) {
                    renate_file();
                    if (PlaylistFrag.getSelected_playlist() == null) {
                        PlaylistFrag.getRecyclerView().setLayoutManager(PlaylistFrag.getLayoutManager());
                        RecyclerView.Adapter adapter = new AdapterPlaylists(PlaylistFrag.context, MainActivity.getMusic_folder().listFiles());
                        PlaylistFrag.getRecyclerView().setAdapter(adapter);
                    } else {
                        PlaylistFrag.getRecyclerView().setLayoutManager(PlaylistFrag.getLayoutManager());
                        RecyclerView.Adapter adapter = new AdapterSongs(PlaylistFrag.context, PlaylistFrag.getSelected_playlist().listFiles());
                        PlaylistFrag.getRecyclerView().setAdapter(adapter);
                    }
                    PlaylistFrag.getRecyclerView().getAdapter().notifyDataSetChanged();
                }
            }
        });

        new_name = view.findViewById(R.id.edit_playlist_name);
        new_name.setHint(file_type + ": name");
        new_name.setText(old_playlist.getName().replace("_", " ").replace(".mp3", ""));
        return builder.create();
    }

    public void set_old_playlist(File old_playlist) {
        this.old_playlist = old_playlist;
    }

    public void renate_file() {
        File new_playlist;
        if (file_type == "Song") {
            new_playlist = new File(old_playlist.getParentFile().getAbsolutePath() + "/" +
                    Helper.format_name(new_name.getText().toString()) + ".mp3");
        } else {
            new_playlist = new File(old_playlist.getParentFile().getAbsolutePath() + "/" +
                    Helper.format_name(new_name.getText().toString()));
        }
        boolean renamed = old_playlist.renameTo(new_playlist);

        if (renamed) {
            Log.d("LOG", "File renamed...");
        } else {
            Log.d("LOG", "File not renamed...");
        }
    }

    public void set_file_type(String file_type) {
        this.file_type = file_type;
    }
}
