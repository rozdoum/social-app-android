package com.rozdoum.socialcomponents.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kristina on 10/31/16.
 */

public class PostsAdapter extends BaseAdapter {

    private List<Post> list = new ArrayList<>();
    private ImageUtil imageUtil;

    public PostsAdapter(Context context) {
        imageUtil = ImageUtil.getInstance(context.getApplicationContext());
    }

    private class ViewHolder {
        ImageView postImageView;
        TextView titleTextView;
        TextView detailsTextView;
        ImageLoader.ImageContainer imageRequest;
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

        if (holder.imageRequest != null) {
            holder.imageRequest.cancelRequest();
        }

        String imageUrl = post.getImagePath();
        holder.imageRequest = imageUtil.getImageThumb(imageUrl, holder.postImageView, R.drawable.ic_stub, R.drawable.ic_stub);

        return convertView;
    }

    public void setList(List<Post> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
