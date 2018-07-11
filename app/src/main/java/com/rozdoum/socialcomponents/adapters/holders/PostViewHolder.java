/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.rozdoum.socialcomponents.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rozdoum.socialcomponents.Constants;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.controllers.LikeController;
import com.rozdoum.socialcomponents.main.base.BaseActivity;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.FormatterUtil;
import com.rozdoum.socialcomponents.utils.GlideApp;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.utils.Utils;

/**
 * Created by alexey on 27.12.16.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = PostViewHolder.class.getSimpleName();

    protected Context context;
    private ImageView postImageView;
    private TextView titleTextView;
    private TextView detailsTextView;
    private TextView likeCounterTextView;
    private ImageView likesImageView;
    private TextView commentsCountTextView;
    private TextView watcherCounterTextView;
    private TextView dateTextView;
    private ImageView authorImageView;
    private ViewGroup likeViewGroup;

    private ProfileManager profileManager;
    protected PostManager postManager;

    private LikeController likeController;
    private BaseActivity baseActivity;

    public PostViewHolder(View view, final OnClickListener onClickListener, BaseActivity activity) {
        this(view, onClickListener, activity, true);
    }

    public PostViewHolder(View view, final OnClickListener onClickListener, BaseActivity activity, boolean isAuthorNeeded) {
        super(view);
        this.context = view.getContext();
        this.baseActivity = activity;

        postImageView = view.findViewById(R.id.postImageView);
        likeCounterTextView = view.findViewById(R.id.likeCounterTextView);
        likesImageView = view.findViewById(R.id.likesImageView);
        commentsCountTextView = view.findViewById(R.id.commentsCountTextView);
        watcherCounterTextView = view.findViewById(R.id.watcherCounterTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        titleTextView = view.findViewById(R.id.titleTextView);
        detailsTextView = view.findViewById(R.id.detailsTextView);
        authorImageView = view.findViewById(R.id.authorImageView);
        likeViewGroup = view.findViewById(R.id.likesContainer);

        authorImageView.setVisibility(isAuthorNeeded ? View.VISIBLE : View.GONE);

        profileManager = ProfileManager.getInstance(context.getApplicationContext());
        postManager = PostManager.getInstance(context.getApplicationContext());

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onItemClick(getAdapterPosition(), v);
            }
        });

        likeViewGroup.setOnClickListener(view1 -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onLikeClick(likeController, position);
            }
        });

        authorImageView.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onAuthorClick(getAdapterPosition(), v);
            }
        });
    }

    public void bindData(Post post) {

        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);

        String title = removeNewLinesDividers(post.getTitle());
        titleTextView.setText(title);
        String description = removeNewLinesDividers(post.getDescription());
        detailsTextView.setText(description);
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        dateTextView.setText(date);

        String imageUrl = post.getImagePath();
        int width = Utils.getDisplayWidth(context);
        int height = (int) context.getResources().getDimension(R.dimen.post_detail_image_height);

        // Displayed and saved to cache image, as needs for post detail.
        ImageUtil.loadImageCenterCrop(GlideApp.with(baseActivity), imageUrl, postImageView, width, height);

        if (post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private String removeNewLinesDividers(String text) {
        int decoratedTextLength = text.length() < Constants.Post.MAX_TEXT_LENGTH_IN_LIST ?
                text.length() : Constants.Post.MAX_TEXT_LENGTH_IN_LIST;
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                if (obj.getPhotoUrl() != null) {
                    if (!baseActivity.isFinishing() && !baseActivity.isDestroyed()) {
                        ImageUtil.loadImage(GlideApp.with(baseActivity), obj.getPhotoUrl(), authorImageView);
                    }
                }
            }
        };
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return exist -> likeController.initLike(exist);
    }

    public interface OnClickListener {
        void onItemClick(int position, View view);

        void onLikeClick(LikeController likeController, int position);

        void onAuthorClick(int position, View view);
    }
}