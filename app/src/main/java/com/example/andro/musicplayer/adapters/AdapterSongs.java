package com.example.andro.musicplayer.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.andro.musicplayer.Helper;
import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.RecyclerClickListener;
import com.example.andro.musicplayer.dialogs.LClickSongDialog;
import com.example.andro.musicplayer.fragments.PlaylistFrag;

import java.io.File;

public class AdapterSongs extends RecyclerView.Adapter<HolderSongs> {

    private static File[] songs;
    private static int selectedPos = RecyclerView.NO_POSITION;
    protected Context context;
    private int last_position = -1;

    public AdapterSongs(Context context, File[] songs) {
        this.context = context;
        AdapterSongs.songs = songs;
    }

    public static void setSelectedPos(int selectedPos) {
        AdapterSongs.selectedPos = selectedPos;
    }

    public static void set_xml_to_playing_song(HolderSongs holder, int position) {
        if (position == selectedPos) {
            holder.card.setCardBackgroundColor(holder.card.getContext().getResources().getColor(R.color.colorAccent));
            holder.song_name.setTextColor(holder.song_name.getContext().getResources().getColor(R.color.cardview_light_background));
            holder.image.setImageResource(R.mipmap.ic_music_note_white_24dp);
        } else {
            holder.card.setCardBackgroundColor(holder.card.getContext().getResources().getColor(R.color.cardview_light_background));
            holder.song_name.setTextColor(holder.song_name.getContext().getResources().getColor(R.color.colorPrimary));
            holder.image.setImageResource(R.mipmap.ic_music_note_black_24dp);
        }
    }

    @Override
    public HolderSongs onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_recycler_item, parent, false);
        return new HolderSongs(view);
    }

    @Override
    public void onBindViewHolder(final HolderSongs holder, int position) {
        holder.itemView.setSelected(true);
        set_xml_to_playing_song(holder, position);
        holder.song_name.setText(Helper.deformat_name(songs[position].getName()));
        set_animation(holder.itemView, position);
        set_holder_click_listener(holder, position);

    }

    public void set_holder_click_listener(final HolderSongs holder, int position) {
        holder.setItemClickListener(new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (isLongClick) {
                    LClickSongDialog dialog = new LClickSongDialog();
                    dialog.setSong_file(songs[position]);
                    dialog.show(PlaylistFrag.getFragment_manager(), "edit playlist dialog");
                } else {
                    if (PlaylistFrag.getMusic_player() != null) {
                        if (PlaylistFrag.getMusic_player().isPlaying()) {
                            PlaylistFrag.getMusic_player().stop();
                        }
                    }
                    notifyItemChanged(selectedPos);
                    selectedPos = holder.getLayoutPosition();
                    notifyItemChanged(selectedPos);
                    set_xml_to_playing_song(holder, selectedPos);
                    notifyDataSetChanged();
                    PlaylistFrag.setIndex_song(selectedPos);
                    PlaylistFrag.start_playing_songs();
                }
            }
        });
    }

    private void set_animation(View viewToAnimate, int position) {
        if (position > last_position) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down);
            animation.setDuration(300);
            if (position < 6)
                animation.setStartOffset(position * 80);
            viewToAnimate.startAnimation(animation);
            last_position = position;
        }
    }

    @Override
    public int getItemCount() {
        return songs.length;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

}

class HolderSongs extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public ImageView image;
    public TextView song_name;
    public CardView card;
    private RecyclerClickListener recycler_click_listener;

    public HolderSongs(View itemView) {
        super(itemView);
        song_name = itemView.findViewById(R.id.song_name);
        image = itemView.findViewById(R.id.image_song);
        card = itemView.findViewById(R.id.cardview_song);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void setItemClickListener(RecyclerClickListener recycler_click_listener) {
        this.recycler_click_listener = recycler_click_listener;
    }

    @Override
    public void onClick(View v) {
        recycler_click_listener.onClick(v, getAdapterPosition(), false);
    }

    @Override
    public boolean onLongClick(View v) {
        recycler_click_listener.onClick(v, getAdapterPosition(), true);
        return true;
    }


}