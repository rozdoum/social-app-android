package com.rozdoum.socialcomponents.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.CommentsAdapter;
import com.rozdoum.socialcomponents.managers.listeners.OnCountChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.ImageUtil;

import java.util.List;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String POST_EXTRA_KEY = "PostDetailsActivity.POST_EXTRA_KEY";
    private static final int ANIMATION_DURATION = 300;

    private EditText commentEditText;
    private Post post;
    private ScrollView scrollView;
    private ViewGroup likesContainer;
    private ImageView likesImageView;
    private TextView commentsLabel;
    private TextView likeCounterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        post = (Post) getIntent().getSerializableExtra(POST_EXTRA_KEY);

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        TextView descriptionEditText = (TextView) findViewById(R.id.descriptionEditText);
        ImageView postImageView = (ImageView) findViewById(R.id.postImageView);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        LinearLayout commentsContainer = (LinearLayout) findViewById(R.id.commentsContainer);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        commentsLabel = (TextView) findViewById(R.id.commentsLabel);
        commentEditText = (EditText) findViewById(R.id.commentEditText);
        ImageButton sendButton = (ImageButton) findViewById(R.id.sendButton);
        likesContainer = (ViewGroup) findViewById(R.id.likesContainer);
        likesImageView = (ImageView) findViewById(R.id.likesImageView);
        likeCounterTextView = (TextView) findViewById(R.id.likeCounterTextView);


        titleTextView.setText(post.getTitle());
        descriptionEditText.setText(post.getDescription());

        String imageUrl = post.getImagePath();

        ImageUtil imageUtil = ImageUtil.getInstance(this);
        imageUtil.getImage(imageUrl, postImageView, progressBar, R.drawable.ic_stub, R.drawable.ic_stub);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        CommentsAdapter commentsAdapter = new CommentsAdapter(commentsContainer);
        ApplicationHelper.getDatabaseHelper().getCommentsList(post.getId(), createOnPostChangedDataListener(commentsAdapter));

        initLikes();
    }

    private OnDataChangedListener<Comment> createOnPostChangedDataListener(final CommentsAdapter commentsAdapter) {
        return new OnDataChangedListener<Comment>() {
            @Override
            public void onListChanged(List<Comment> list) {
                commentsAdapter.setList(list);
                if (list.size() > 0) {
                    commentsLabel.setVisibility(View.VISIBLE);
                    commentsLabel.setText(String.format(getString(R.string.label_comments), list.size()));
                } else {
                    commentsLabel.setVisibility(View.GONE);
                }
            }
        };
    }

    private OnCountChangedListener<Like> createOnLikeCountChangedListener() {
        return new OnCountChangedListener<Like>() {
            @Override
            public void onCountChanged(long count) {
                String likeTextFormat = getString(R.string.label_likes);
                likeCounterTextView.setText(String.format(likeTextFormat, count));
            }
        };
    }

    private void initLikes() {
        ApplicationHelper.getDatabaseHelper().getLikesCount(post.getId(), createOnLikeCountChangedListener());

        likesContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isActivated = !likesImageView.isActivated();
                likesImageView.setActivated(isActivated);
                startAnimateLikeButton(AnimationType.BOUNCE_ANIM);
                if (isActivated) {
                    addLike();
                } else {
                    removeLike();
                }
            }
        });
    }

    private void startAnimateLikeButton(AnimationType animationType) {
        switch (animationType) {
            case BOUNCE_ANIM:
                bounceAnimateImageView();
                break;
            case COLOR_ANIM:
                colorAnimateImageView();
                break;
        }
    }

    public void colorAnimateImageView() {
        final int activatedColor = getResources().getColor(R.color.like_icon_activated);

        final ValueAnimator colorAnim = likesImageView.isActivated() ? ObjectAnimator.ofFloat(0f, 1f)
                : ObjectAnimator.ofFloat(1f, 0f);
        colorAnim.setDuration(ANIMATION_DURATION);
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mul = (Float) animation.getAnimatedValue();
                int alpha = adjustAlpha(activatedColor, mul);
                likesImageView.setColorFilter(alpha, PorterDuff.Mode.SRC_ATOP);
                if (mul == 0.0) {
                    likesImageView.setColorFilter(null);
                }
            }
        });

        colorAnim.start();
    }

    public void bounceAnimateImageView() {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(likesImageView, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(ANIMATION_DURATION);
        bounceAnimX.setInterpolator(new BounceInterpolator());

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(likesImageView, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(ANIMATION_DURATION);
        bounceAnimY.setInterpolator(new BounceInterpolator());
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                likesImageView.setImageResource(likesImageView.isActivated() ? R.drawable.ic_favorite_24px
                        : R.drawable.ic_favorite_border_24px);
            }
        });

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY);
        animatorSet.start();
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private void addLike() {
        ApplicationHelper.getDatabaseHelper().createOrUpdateLike(post.getId());
    }

    private void removeLike() {
    }

    private void sendComment() {
        String commentText = commentEditText.getText().toString();

        if (commentText.length() > 0) {
            ApplicationHelper.getDatabaseHelper().createOrUpdateComment(commentText, post.getId());
            commentEditText.setText(null);
            commentEditText.clearFocus();
            hideKeyBoard();
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void hideKeyBoard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    enum AnimationType {
        COLOR_ANIM, BOUNCE_ANIM
    }
}
