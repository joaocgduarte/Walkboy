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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class AdapterRecommendationsSearch extends RecyclerView.Adapter<HolderRecommendationsSearch> {

    protected static List<String> artist_name_list;
    protected static List<String> image_artist_list;
    protected Context context;
    protected int last_position;

    public AdapterRecommendationsSearch(List<String> artist_name_list, List<String> image_artist_list, Context context) {
        AdapterRecommendationsSearch.artist_name_list = artist_name_list;
        AdapterRecommendationsSearch.image_artist_list = image_artist_list;
        this.context = context;
        last_position = -1;
    }

    @Override
    public HolderRecommendationsSearch onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommended_artists_search_item, parent, false);
        return new HolderRecommendationsSearch(view);
    }

    @Override
    public void onBindViewHolder(HolderRecommendationsSearch holder, int position) {
        Glide.with(context).asBitmap().load(image_artist_list.get(position)).into(holder.image);
        holder.text.setText(artist_name_list.get(position));
        set_holder_on_click_listener(holder, position);
        set_animation(holder.itemView, position);
    }

    private void set_animation(View viewToAnimate, int position) {
        if (position > last_position) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            animation.setDuration(300);
            if (position < 3) {
                animation.setStartOffset(position * 80);
            }
            viewToAnimate.startAnimation(animation);
            last_position = position;
        }
    }

    private void set_holder_on_click_listener(final HolderRecommendationsSearch holder, int position) {
        holder.setItemClickListener(new RecyclerClickListener() {
            public void onClick(View view, int position, boolean isLongClick) {
                LastFMManager lastfm = new LastFMManager(context, true, false, false, false);
                lastfm.setClicked_artist(holder.text.getText().toString());
                lastfm.execute();
            }
        });
    }

    @Override
    public int getItemCount() {
        return artist_name_list.size();
    }

}

class HolderRecommendationsSearch extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    CircleImageView image;
    TextView text;
    private RecyclerClickListener recycler_click_listener;

    public HolderRecommendationsSearch(View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.img_artist_one);
        text = itemView.findViewById(R.id.text_artist_one);
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
