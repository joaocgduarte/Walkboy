package com.example.andro.musicplayer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.RecyclerClickListener;
import com.example.andro.musicplayer.asynctasks.LastFMManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterRecommendations extends RecyclerView.Adapter<HolderRecommendations> {

    private List<String> artist_name_list = new ArrayList<>();
    private List<String> artist_image_list = new ArrayList<>();
    private Context context;
    private int last_position = -1;

    public AdapterRecommendations(List<String> artist_name_list, List<String> artist_image_list, Context context) {
        this.artist_name_list = artist_name_list;
        this.artist_image_list = artist_image_list;
        this.context = context;
    }

    public HolderRecommendations onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommended_artists_item, null);
        return new HolderRecommendations(view);
    }

    @Override
    public void onBindViewHolder(HolderRecommendations holder, int position) {
        Glide.with(context).asBitmap().load(artist_image_list.get(position)).into(holder.getArtist_image());
        holder.getArtist_name().setText(artist_name_list.get(position));
        set_holder_on_click_listener(holder, position);
        set_animation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return artist_name_list.size();
    }

    private void set_holder_on_click_listener(final HolderRecommendations holder, int position) {
        holder.setItemClickListener(new RecyclerClickListener() {
            public void onClick(View view, int position, boolean isLongClick) {
                LastFMManager lastfm = new LastFMManager(context, true, false, false, false);
                lastfm.setClicked_artist(holder.artist_name.getText().toString());
                lastfm.execute();

            }
        });
    }

    private void set_animation(View viewToAnimate, int position) {
        if (position > last_position) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down);
            animation.setDuration(300);
            if (position < 15) {
                animation.setStartOffset(position * 80);
            }
            viewToAnimate.startAnimation(animation);
            last_position = position;
        }
    }
}

class HolderRecommendations extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    protected CircleImageView artist_image;
    protected TextView artist_name;
    private RecyclerClickListener recycler_click_listener;

    public HolderRecommendations(View itemView) {
        super(itemView);
        artist_image = itemView.findViewById(R.id.img_artist);
        artist_name = itemView.findViewById(R.id.text_artist);
        artist_name.setSelected(true);
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

    public CircleImageView getArtist_image() {
        return artist_image;
    }

    public TextView getArtist_name() {
        return artist_name;
    }
}
