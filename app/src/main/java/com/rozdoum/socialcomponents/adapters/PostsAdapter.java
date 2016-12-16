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
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kristina on 10/31/16.
 */

public class PostsAdapter extends BaseAdapter {

    private List<Post> list = new ArrayList<>();
    private ImageUtil imageUtil;
    private ProfileManager profileManager;

    public PostsAdapter(Context context) {
        imageUtil = ImageUtil.getInstance(context.getApplicationContext());
        profileManager = ProfileManager.getInstance(context);
    }

    private class ViewHolder {
        ImageView postImageView;
        TextView titleTextView;
        TextView detailsTextView;
        TextView likeCounterTextView;
        TextView commentsCountTextView;
        ImageView authorImageView;

        ImageLoader.ImageContainer imageRequest;
        ImageLoader.ImageContainer authorImageRequest;
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
            holder.likeCounterTextView = (TextView) convertView.findViewById(R.id.likesCountTextView);
            holder.commentsCountTextView = (TextView) convertView.findViewById(R.id.commentsCountTextView);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
            holder.detailsTextView = (TextView) convertView.findViewById(R.id.detailsTextView);
            holder.authorImageView = (ImageView) convertView.findViewById(R.id.authorImageView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Post post = list.get(position);

        holder.titleTextView.setText(post.getTitle());
        holder.detailsTextView.setText(post.getDescription());
        holder.likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        holder.commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));

        if (holder.imageRequest != null) {
            holder.imageRequest.cancelRequest();
        }

        String imageUrl = post.getImagePath();
        holder.imageRequest = imageUtil.getImageThumb(imageUrl, holder.postImageView, R.drawable.ic_stub, R.drawable.ic_stub);

        if (post.getAuthorId() != null) {
            holder.authorImageView.setVisibility(View.VISIBLE);
            Object imageViewTag = holder.authorImageView.getTag();

            if (!post.getAuthorId().equals(imageViewTag)) {
                cancelLoadingAuthorImage(holder);
                holder.authorImageView.setTag(post.getAuthorId());
                profileManager.getProfile(post.getAuthorId(), createProfileChangeListener(holder));
            }
        }

        return convertView;
    }

    private void cancelLoadingAuthorImage(ViewHolder holder) {
        if (holder.authorImageRequest != null) {
            holder.authorImageRequest.cancelRequest();
        }
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ViewHolder holder) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                if (obj.getPhotoUrl() != null) {
                    imageUtil.getImageThumb(obj.getPhotoUrl(),
                            holder.authorImageView, R.drawable.ic_stub, R.drawable.ic_stub, true);
                }
            }
        };
    }

    public void setList(List<Post> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
