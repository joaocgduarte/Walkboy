package com.example.andro.musicplayer.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.andro.musicplayer.Helper;
import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.RecyclerClickListener;
import com.example.andro.musicplayer.dialogs.EditFileDialog;
import com.example.andro.musicplayer.fragments.PlaylistFrag;

import java.io.File;


public class AdapterPlaylists extends RecyclerView.Adapter<HolderPlaylist> {

    protected Context context;
    protected File[] playlists;
    protected int last_position = -1;

    public AdapterPlaylists(Context context, File[] playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    public HolderPlaylist onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_recycler_item, parent, false);
        return new HolderPlaylist(view);
    }

    public void onBindViewHolder(final HolderPlaylist holder, int position) {
        holder.itemView.setSelected(true);
        holder.playlist_name.setText(Helper.deformat_name(playlists[position].getName()));
        holder.playlist_description.setText("Playlist - " + (playlists[position].listFiles().length + " songs"));
        final String path = playlists[position].getAbsolutePath();
        setAnimation(holder.itemView, position);
        set_holder_on_click_listener(holder, path);

    }

    private void set_holder_on_click_listener(HolderPlaylist holder, final String path) {
        holder.setItemClickListener(new RecyclerClickListener() {
            @Override
            public void onClick(View view, final int position, boolean isLongClick) {
                if (isLongClick) {
                    long_click_listener(position);
                } else {
                    PlaylistFrag.update_recycler_to_songs(new File(path));
                }
            }
        });
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > last_position) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down);
            animation.setDuration(300);
            if (position < 4)
                animation.setStartOffset(position * 80);
            viewToAnimate.startAnimation(animation);
            last_position = position;
        }
    }

    public int getItemCount() {
        return playlists.length;
    }

    public void long_click_listener(final int position) {
        AlertDialog.Builder builder_initial_dialog = new AlertDialog.Builder(context);
        builder_initial_dialog.setTitle("Playlist: " + playlists[position].getName().replace("_", " "))
                .setMessage("What do you want to do with this playlist?")
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edit_playlist(position);
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        initiate_confirmation_dialog(position);
                    }
                });
        AlertDialog dialog = builder_initial_dialog.create();
        dialog.show();
    }

    public void edit_playlist(int position) {
        if (position > 0) {
            EditFileDialog playlistDialog = new EditFileDialog();
            playlistDialog.set_old_playlist(playlists[position]);
            playlistDialog.set_file_type("Playlist");
            playlistDialog.show(PlaylistFrag.getFragment_manager(), "edit playlist dialog");
        }
    }

    public void initiate_confirmation_dialog(final int position) {
        AlertDialog.Builder confirmation_dialog_builder = new AlertDialog.Builder(context);
        confirmation_dialog_builder.setTitle("Are you sure?")
                .setMessage("If you delete this playlist, all of the songs inside it will be deleted!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete_playlist(position);
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

    public void delete_playlist(int position) {
        if (position > 0) {
            File[] playlist_songs = playlists[position].listFiles();
            for (int i = 0; i < playlist_songs.length; i++) {
                playlist_songs[i].delete();
                playlist_songs[i] = null;
            }
            File[] new_playlists = new File[playlists.length - 1];
            for (int i = 0; i < new_playlists.length; i++) {
                for (int j = i; j < playlists.length; j++) {
                    if (j != position) {
                        new_playlists[i] = playlists[j];
                        break;
                    }
                }
            }
            playlists[position].delete();
            playlists = new_playlists;
            notifyDataSetChanged();
        }
    }
}

class HolderPlaylist extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public TextView playlist_name;
    public TextView playlist_description;
    private RecyclerClickListener recycler_click_listener;

    public HolderPlaylist(View itemView) {
        super(itemView);
        playlist_name = itemView.findViewById(R.id.playlist_item_name);
        playlist_description = itemView.findViewById(R.id.description);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void setItemClickListener(RecyclerClickListener recycler_click_listener) {
        this.recycler_click_listener = recycler_click_listener;
    }

    public void onClick(View v) {
        recycler_click_listener.onClick(v, getAdapterPosition(), false);
    }

    public boolean onLongClick(View v) {
        recycler_click_listener.onClick(v, getAdapterPosition(), true);
        return true;
    }

}

