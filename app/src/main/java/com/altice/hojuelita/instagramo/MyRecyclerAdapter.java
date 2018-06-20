package com.altice.hojuelita.instagramo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyRecycleItemViewHolder> {
    private final Context context;
    private List<Noticia> noticias;

    MyRecyclerAdapter(Context context, List<Noticia> noticias) {
        this.context = context;
        this.noticias = noticias;
    }

    @NonNull
    @Override
    public MyRecycleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_layout, parent, false);
        return new MyRecycleItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecycleItemViewHolder holder, int position) {
        TextView descriptionTextView = holder.itemView.findViewById(R.id.descripcion);
        descriptionTextView.setText(noticias.get(position).getDescription());
        TextView locationTextView = holder.itemView.findViewById(R.id.locationTV);
        locationTextView.setText(noticias.get(position).getLocation());
        ImageView picture = holder.itemView.findViewById(R.id.picture);

        new MyAsyncTask(picture).execute(noticias.get(position).getUrl());
    }

    @Override
    public int getItemCount() {
        return noticias.size();
    }

    static class MyRecycleItemViewHolder extends RecyclerView.ViewHolder {
        MyRecycleItemViewHolder(View itemView) {
            super(itemView);
        }
    }

}
