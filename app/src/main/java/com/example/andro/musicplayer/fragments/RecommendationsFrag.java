package com.example.andro.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.andro.musicplayer.R;
import com.example.andro.musicplayer.adapters.AdapterRecommendations;
import com.example.andro.musicplayer.asynctasks.LastFMManager;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsFrag extends Fragment {

    protected static RecyclerView recycler_view;
    protected static GridLayoutManager grid_layout_manager;
    protected static Context context;
    protected static List<String> artists_images;
    protected static List<String> artists_names;

    public static void update_recommendations_list() {
        recycler_view.setLayoutManager(grid_layout_manager);
        AdapterRecommendations adapter_rec_view = new AdapterRecommendations(artists_names, artists_images, context);
        recycler_view.setAdapter(adapter_rec_view);
        recycler_view.getAdapter().notifyDataSetChanged();
    }

    public static RecyclerView getRecycler_view() {
        return recycler_view;
    }

    public static List<String> getArtists_images() {
        return artists_images;
    }

    public void setArtists_images(List<String> artists_images) {
        RecommendationsFrag.artists_images = artists_images;
    }

    public static List<String> getArtists_names() {
        return artists_names;
    }

    public void setArtists_names(List<String> artists_names) {
        RecommendationsFrag.artists_names = artists_names;
    }

    public void initiate_variables() {
        recycler_view = getView().findViewById(R.id.recycler_view_recommendations);
        grid_layout_manager = new GridLayoutManager(context, 3);
        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(grid_layout_manager);
        artists_images = new ArrayList<>();
        artists_names = new ArrayList<>();
        initiate_adapter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        RecommendationsFrag.context = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        return inflater.inflate(R.layout.recommendations_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initiate_variables();
        LastFMManager recommendations = new LastFMManager(context, false, true, false, false);
        recommendations.execute();
        recycler_view.getAdapter().notifyDataSetChanged();
        update_recommendations_list();
    }

    public void initiate_adapter() {
        AdapterRecommendations adapter = new AdapterRecommendations(artists_names, artists_images, context);
        recycler_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


}
