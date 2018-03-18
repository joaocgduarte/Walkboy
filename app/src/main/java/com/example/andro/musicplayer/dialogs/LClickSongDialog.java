package com.example.andro.musicplayer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.fragments.PlaylistFrag;

import java.io.File;

public class LClickSongDialog extends AppCompatDialogFragment {

    private TextView edit_button;
    private TextView move_button;
    private TextView delete_button;
    private File song_file;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.song_long_click_dialog, null);

        edit_button = view.findViewById(R.id.edit_button);
        move_button = view.findViewById(R.id.move_button);
        delete_button = view.findViewById(R.id.delete_button);

        edit_button.setPaintFlags(edit_button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        move_button.setPaintFlags(move_button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        delete_button.setPaintFlags(delete_button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        builder.setView(view)
                .setTitle(song_file.getName().replace("_", " ").replace(".mp3", " "))
                .setMessage("What do you want to do with this song?");

        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                EditFileDialog playlistDialog = new EditFileDialog();
                playlistDialog.set_old_playlist(song_file);
                playlistDialog.set_file_type("Song");
                playlistDialog.show(PlaylistFrag.getFragment_manager(), "edit song dialog");
            }
        });

        move_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                MoveSongDialog dialog = new MoveSongDialog();
                dialog.setSelected_song(song_file);
                dialog.show(PlaylistFrag.getFragment_manager(), "Move song dialog");
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                AlertDialog.Builder confirmation_dialog_builder = new AlertDialog.Builder(PlaylistFrag.context);
                confirmation_dialog_builder.setTitle("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                song_file.delete();
                                PlaylistFrag.initiate_recycler(PlaylistFrag.getSelected_playlist().listFiles(), false);
                                PlaylistFrag.getRecyclerView().getAdapter().notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog confirmation_dialog = confirmation_dialog_builder.create();
                confirmation_dialog.show();
            }
        });

        return builder.create();
    }


    public void setSong_file(File song_file) {
        this.song_file = song_file;
    }
}
