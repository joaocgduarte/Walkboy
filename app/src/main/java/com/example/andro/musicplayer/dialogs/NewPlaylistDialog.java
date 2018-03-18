package com.example.andro.musicplayer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.andro.musicplayer.MainActivity;
import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.adapters.AdapterPlaylists;
import com.example.andro.musicplayer.fragments.PlaylistFrag;

import java.io.File;

public class NewPlaylistDialog extends AppCompatDialogFragment {
    private EditText playlist_name;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_playlist_dialog, null);

        builder.setView(view)
                .setTitle("Create a new playlist")
                .setMessage("NOTE: '.' and '/' are invalid characters!")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!playlist_name.getText().toString().contains(".") && !playlist_name.getText().toString().contains("/")) {
                    File new_playlist = new File(MainActivity.getMusic_folder().getAbsolutePath() + "/" + playlist_name.getText().toString());
                    if (!new_playlist.exists()) {
                        if (new_playlist.mkdir()) ; //directory is created;
                    }
                    if (PlaylistFrag.getSelected_playlist() == null) {
                        PlaylistFrag.getRecyclerView().setLayoutManager(PlaylistFrag.getLayoutManager());
                        RecyclerView.Adapter adapter = new AdapterPlaylists(PlaylistFrag.context, MainActivity.getMusic_folder().listFiles());
                        PlaylistFrag.getRecyclerView().setAdapter(adapter);
                        PlaylistFrag.getRecyclerView().getAdapter().notifyDataSetChanged();
                    }
                }
            }
        });

        playlist_name = view.findViewById(R.id.new_playlist_name);
        return builder.create();
    }
}
