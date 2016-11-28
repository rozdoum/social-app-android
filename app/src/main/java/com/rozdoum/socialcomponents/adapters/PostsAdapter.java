package com.rozdoum.socialcomponents.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private List<Post> list = new ArrayList<>();
    private ImageUtil imageUtil;
    private OnItemClickListener onItemClickListener;

    public PostsAdapter(Context context) {
        imageUtil = ImageUtil.getInstance(context.getApplicationContext());
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        TextView titleTextView;
        TextView detailsTextView;
        ImageLoader.ImageContainer imageRequest;

        PostViewHolder(View view) {
            super(view);

            postImageView = (ImageView) view.findViewById(R.id.postImageView);
            titleTextView = (TextView) view.findViewById(R.id.titleTextView);
            detailsTextView = (TextView) view.findViewById(R.id.detailsTextView);

            if (onItemClickListener != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemClick(getItemByPosition(getAdapterPosition()));
                    }
                });
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item_list_view, parent, false);

        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PostViewHolder holder, int position) {
        Post post = list.get(position);

        holder.titleTextView.setText(post.getTitle());
        holder.detailsTextView.setText(post.getDescription());


        if (holder.imageRequest != null) {
            holder.imageRequest.cancelRequest();
        }

        String imageUrl = post.getImagePath();
        holder.imageRequest = imageUtil.getImageThumb(imageUrl, holder.postImageView, R.drawable.ic_stub, R.drawable.ic_stub);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private Post getItemByPosition(int position) {
        return list.get(position);
    }

    public void setList(List<Post> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }
}
