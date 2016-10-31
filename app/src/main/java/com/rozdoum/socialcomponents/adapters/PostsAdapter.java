package com.rozdoum.socialcomponents.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kristina on 10/31/16.
 */

public class PostsAdapter extends BaseAdapter {

    private List<Post> list = new ArrayList<>();

    class ViewHolder {
        ImageView postImageView;
        TextView titleTextView;
        TextView detailsTextView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_list_view, parent, false);

            holder.postImageView = (ImageView) convertView.findViewById(R.id.postImageView);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
            holder.detailsTextView = (TextView) convertView.findViewById(R.id.detailsTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Post post = list.get(position);

        holder.titleTextView.setText(post.getTitle());
        holder.detailsTextView.setText(post.getDescription());

        return convertView;
    }

    public void setList(List<Post> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
