package com.rozdoum.socialcomponents.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.CommentsAdapter;
import com.rozdoum.socialcomponents.enums.ProfileStatus;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.ImageUtil;

import java.util.List;

public class PostDetailsActivity extends BaseActivity {

    public static final String POST_EXTRA_KEY = "PostDetailsActivity.POST_EXTRA_KEY";
    private static final int ANIMATION_DURATION = 300;

    private EditText commentEditText;
    private Post post;
    private ScrollView scrollView;
    private ViewGroup likesContainer;
    private ImageView likesImageView;
    private TextView commentsLabel;
    private TextView likeCounterTextView;
    private ImageView authorImageView;

    private AnimationType likeAnimationType;
    private boolean isLiked = false;
    private boolean likeIconInitialized = false;

    private String postId;

    private ProfileManager profileManager;
    private ImageUtil imageUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        profileManager = ProfileManager.getInstance(this);
        imageUtil = ImageUtil.getInstance(this);

        post = (Post) getIntent().getSerializableExtra(POST_EXTRA_KEY);
        postId = post.getId();

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        TextView descriptionEditText = (TextView) findViewById(R.id.descriptionEditText);
        final ImageView postImageView = (ImageView) findViewById(R.id.postImageView);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        LinearLayout commentsContainer = (LinearLayout) findViewById(R.id.commentsContainer);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        commentsLabel = (TextView) findViewById(R.id.commentsLabel);
        commentEditText = (EditText) findViewById(R.id.commentEditText);
        ImageButton sendButton = (ImageButton) findViewById(R.id.sendButton);
        likesContainer = (ViewGroup) findViewById(R.id.likesContainer);
        likesImageView = (ImageView) findViewById(R.id.likesImageView);
        authorImageView = (ImageView) findViewById(R.id.authorImageView);
        likeCounterTextView = (TextView) findViewById(R.id.likeCounterTextView);

        titleTextView.setText(post.getTitle());
        descriptionEditText.setText(post.getDescription());

        String imageUrl = post.getImagePath();
        imageUtil.getFullImage(imageUrl, postImageView, progressBar, R.drawable.ic_stub);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileStatus profileStatus = ProfileManager.getInstance(PostDetailsActivity.this).checkProfile();

                if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                    sendComment();
                } else {
                    doAuthorization(profileStatus);
                }
            }
        });

        CommentsAdapter commentsAdapter = new CommentsAdapter(commentsContainer);
        ApplicationHelper.getDatabaseHelper().getCommentsList(post.getId(), createOnPostChangedDataListener(commentsAdapter));

        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageDetailScreen();
            }
        });

        initLikes();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                post = dataSnapshot.getValue(Post.class);
                post.setId(postId);
                updateValues();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference().child("posts").child(postId);
        postReference.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initLikeButtonState();
    }

    private void updateValues() {
        commentsLabel.setVisibility(View.VISIBLE);
        commentsLabel.setText(String.format(getString(R.string.label_comments), post.getCommentsCount()));

        String likeTextFormat = getString(R.string.label_likes);
        likeCounterTextView.setText(String.format(likeTextFormat, post.getLikesCount()));

        if (post.getAuthorId() != null) {
            profileManager.getProfile(post.getAuthorId(), createProfileChangeListener());
        }
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                if (obj.getPhotoUrl() != null) {
                    imageUtil.getImageThumb(obj.getPhotoUrl(),
                            authorImageView, R.drawable.ic_stub, R.drawable.ic_stub, true);
                }
            }
        };
    }

    private OnDataChangedListener<Comment> createOnPostChangedDataListener(final CommentsAdapter commentsAdapter) {
        return new OnDataChangedListener<Comment>() {
            @Override
            public void onListChanged(List<Comment> list) {
                commentsAdapter.setList(list);
            }
        };
    }

    private void openImageDetailScreen() {
        Intent intent = new Intent(this, ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_URL_EXTRA_KEY, post.getImagePath());
        startActivity(intent);
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                if (!likeIconInitialized) {
                    likesImageView.setImageResource(exist ? R.drawable.ic_favorite_24px : R.drawable.ic_favorite_border_24px);
                    likeIconInitialized = true;
                }

                isLiked = exist;
            }
        };
    }

    private void initLikeButtonState() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            ApplicationHelper.getDatabaseHelper().hasCurrentUserLike(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private void initLikes() {
        //set default animation type
        likeAnimationType = AnimationType.BOUNCE_ANIM;

        likesContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileStatus profileStatus = ProfileManager.getInstance(PostDetailsActivity.this).checkProfile();

                if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                    likeClickAction();
                } else {
                    doAuthorization(profileStatus);
                }
            }
        });

        //long click for changing animation
        likesContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (likeAnimationType == AnimationType.BOUNCE_ANIM) {
                    likeAnimationType = AnimationType.COLOR_ANIM;
                } else {
                    likeAnimationType = AnimationType.BOUNCE_ANIM;
                }

                Snackbar snackbar = Snackbar
                        .make(likesContainer, "Animation was changed", Snackbar.LENGTH_LONG);

                snackbar.show();
                return true;
            }
        });
    }

    private void likeClickAction() {
        startAnimateLikeButton(likeAnimationType);

        if (!isLiked) {
            addLike();
        } else {
            removeLike();
        }
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

        final ValueAnimator colorAnim = !isLiked ? ObjectAnimator.ofFloat(0f, 1f)
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
                likesImageView.setImageResource(!isLiked ? R.drawable.ic_favorite_24px
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
        ApplicationHelper.getDatabaseHelper().removeLike(post.getId());
    }

    private void sendComment() {
        String commentText = commentEditText.getText().toString();

        if (commentText.length() > 0) {
            ApplicationHelper.getDatabaseHelper().createOrUpdateComment(commentText, post.getId());
            commentEditText.setText(null);
            commentEditText.clearFocus();
            hideKeyBoard();
            scrollView.fullScroll(ScrollView.FOCUS_UP);
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
