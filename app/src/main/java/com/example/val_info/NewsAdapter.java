package com.example.val_info;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private List<NewsAdmin> newsList;

    public NewsAdapter(List<NewsAdmin> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsAdmin news = newsList.get(position);
        holder.newsDate.setText(news.getDate());
        holder.newsDescription.setText(news.getDescription());
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView newsDate;
        TextView newsDescription;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsDate = itemView.findViewById(R.id.news_date);
            newsDescription = itemView.findViewById(R.id.news_description);
        }
    }
}
