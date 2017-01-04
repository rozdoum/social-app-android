package com.rozdoum.socialcomponents.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.controllers.LikeController;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.ImageUtil;

/**
 * Created by alexey on 27.12.16.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {
    private Context context;
    private ImageView postImageView;
    private TextView titleTextView;
    private TextView detailsTextView;
    private TextView likeCounterTextView;
    private ImageView likesImageView;
    private TextView commentsCountTextView;
    private TextView dateTextView;
    private ImageView authorImageView;
    private ViewGroup likeViewGroup;

    private ImageLoader.ImageContainer imageRequest;
    private ImageLoader.ImageContainer authorImageRequest;

    private ImageUtil imageUtil;
    private ProfileManager profileManager;
    private PostManager postManager;

    private boolean isAuthorNeeded;
    private LikeController likeController;

    public PostViewHolder(View view, final OnClickListener onClickListener) {
        this(view, onClickListener, true);
    }

    public PostViewHolder(View view, final OnClickListener onClickListener, boolean isAuthorNeeded) {
        super(view);

        this.isAuthorNeeded = isAuthorNeeded;

        this.context = view.getContext();
        postImageView = (ImageView) view.findViewById(R.id.postImageView);
        likeCounterTextView = (TextView) view.findViewById(R.id.likeCountTextView);
        likesImageView = (ImageView) view.findViewById(R.id.likesImageView);
        commentsCountTextView = (TextView) view.findViewById(R.id.commentsCountTextView);
        dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        detailsTextView = (TextView) view.findViewById(R.id.detailsTextView);
        authorImageView = (ImageView) view.findViewById(R.id.authorImageView);
        likeViewGroup = (ViewGroup) view.findViewById(R.id.likesContainer);

        imageUtil = ImageUtil.getInstance(view.getContext().getApplicationContext());
        profileManager = ProfileManager.getInstance(view.getContext().getApplicationContext());
        postManager = PostManager.getInstance(view.getContext().getApplicationContext());

        if (onClickListener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onItemClick(getAdapterPosition());
                }
            });
        }

        likeViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onLikeClick(likeController, getAdapterPosition());
            }
        });
    }

    public void bindData(Post post) {

        likeController = new LikeController(context, post.getId(), likeCounterTextView, likesImageView, true);

        titleTextView.setText(post.getTitle());
        detailsTextView.setText(post.getDescription());
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));

        long now = System.currentTimeMillis();
        CharSequence date = DateUtils.getRelativeTimeSpanString(post.getCreatedDate(), now, DateUtils.HOUR_IN_MILLIS);
        dateTextView.setText(date);

        if (imageRequest != null) {
            imageRequest.cancelRequest();
        }

        String imageUrl = post.getImagePath();
        imageRequest = imageUtil.getImageThumb(imageUrl, postImageView, R.drawable.ic_stub, R.drawable.ic_stub);

        if (isAuthorNeeded && post.getAuthorId() != null) {
            authorImageView.setVisibility(View.VISIBLE);
            Object imageViewTag = authorImageView.getTag();

            if (!post.getAuthorId().equals(imageViewTag)) {
                cancelLoadingAuthorImage();
                authorImageView.setTag(post.getAuthorId());
                profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView));
            }
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLike(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private void cancelLoadingAuthorImage() {
        if (authorImageRequest != null) {
            authorImageRequest.cancelRequest();
        }
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                if (obj.getPhotoUrl() != null) {
                    authorImageRequest = imageUtil.getImageThumb(obj.getPhotoUrl(),
                            authorImageView, R.drawable.ic_stub, R.drawable.ic_stub);
                }
            }
        };
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                likeController.initLike(exist);
            }
        };
    }

    public interface OnClickListener {
        void onItemClick(int position);
        void onLikeClick(LikeController likeController, int position);
    }
}