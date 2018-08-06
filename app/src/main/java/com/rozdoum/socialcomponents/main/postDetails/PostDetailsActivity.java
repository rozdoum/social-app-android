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

package com.rozdoum.socialcomponents.main.postDetails;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.CommentsAdapter;
import com.rozdoum.socialcomponents.controllers.LikeController;
import com.rozdoum.socialcomponents.dialogs.EditCommentDialog;
import com.rozdoum.socialcomponents.enums.PostStatus;
import com.rozdoum.socialcomponents.listeners.CustomTransitionListener;
import com.rozdoum.socialcomponents.main.base.BaseActivity;
import com.rozdoum.socialcomponents.main.imageDetail.ImageDetailActivity;
import com.rozdoum.socialcomponents.main.post.editPost.EditPostActivity;
import com.rozdoum.socialcomponents.main.profile.ProfileActivity;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.AnimationUtils;
import com.rozdoum.socialcomponents.utils.FormatterUtil;
import com.rozdoum.socialcomponents.utils.GlideApp;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.utils.Utils;

import java.util.List;

public class PostDetailsActivity extends BaseActivity<PostDetailsView, PostDetailsPresenter> implements PostDetailsView, EditCommentDialog.CommentDialogCallback {

    public static final String POST_ID_EXTRA_KEY = "PostDetailsActivity.POST_ID_EXTRA_KEY";
    public static final String AUTHOR_ANIMATION_NEEDED_EXTRA_KEY = "PostDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY";
    public static final int UPDATE_POST_REQUEST = 1;
    public static final String POST_STATUS_EXTRA_KEY = "PostDetailsActivity.POST_STATUS_EXTRA_KEY";

    private EditText commentEditText;
    @Nullable
    private ScrollView scrollView;
    private ViewGroup likesContainer;
    private ImageView likesImageView;
    private TextView commentsLabel;
    private TextView likeCounterTextView;
    private TextView commentsCountTextView;
    private TextView watcherCounterTextView;
    private TextView authorTextView;
    private TextView dateTextView;
    private ImageView authorImageView;
    private ProgressBar progressBar;
    private ImageView postImageView;
    private TextView titleTextView;
    private TextView descriptionEditText;
    private ProgressBar commentsProgressBar;
    private RecyclerView commentsRecyclerView;
    private TextView warningCommentsTextView;


    private MenuItem complainActionMenuItem;
    private MenuItem editActionMenuItem;
    private MenuItem deleteActionMenuItem;

    private String postId;

    private PostManager postManager;
    private LikeController likeController;
    private boolean authorAnimationInProgress = false;

    private boolean isAuthorAnimationRequired;
    private CommentsAdapter commentsAdapter;
    private ActionMode mActionMode;
    private boolean isEnterTransitionFinished = false;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        postManager = PostManager.getInstance(this);

        isAuthorAnimationRequired = getIntent().getBooleanExtra(AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, false);
        postId = getIntent().getStringExtra(POST_ID_EXTRA_KEY);

        incrementWatchersCount();

        titleTextView = findViewById(R.id.titleTextView);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        postImageView = findViewById(R.id.postImageView);
        progressBar = findViewById(R.id.progressBar);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        scrollView = findViewById(R.id.scrollView);
        commentsLabel = findViewById(R.id.commentsLabel);
        commentEditText = findViewById(R.id.commentEditText);
        likesContainer = findViewById(R.id.likesContainer);
        likesImageView = findViewById(R.id.likesImageView);
        authorImageView = findViewById(R.id.authorImageView);
        authorTextView = findViewById(R.id.authorTextView);
        likeCounterTextView = findViewById(R.id.likeCounterTextView);
        commentsCountTextView = findViewById(R.id.commentsCountTextView);
        watcherCounterTextView = findViewById(R.id.watcherCounterTextView);
        dateTextView = findViewById(R.id.dateTextView);
        commentsProgressBar = findViewById(R.id.commentsProgressBar);
        warningCommentsTextView = findViewById(R.id.warningCommentsTextView);
        sendButton = findViewById(R.id.sendButton);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isAuthorAnimationRequired) {
            authorImageView.setScaleX(0);
            authorImageView.setScaleY(0);

            // Add a listener to get noticed when the transition ends to animate the fab button
            getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    //disable execution for exit transition
                    if (!isEnterTransitionFinished) {
                        isEnterTransitionFinished = true;
                        com.rozdoum.socialcomponents.utils.AnimationUtils.showViewByScale(authorImageView)
                                .setListener(authorAnimatorListener)
                                .start();
                    }
                }
            });
        }

        initRecyclerView();
        initListeners();

        presenter.loadPost(postId);
        supportPostponeEnterTransition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postManager.closeListeners(this);
    }

    @NonNull
    @Override
    public PostDetailsPresenter createPresenter() {
        if (presenter == null) {
            return new PostDetailsPresenter(this);
        }
        return presenter;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && isAuthorAnimationRequired
                && !authorAnimationInProgress
                && !AnimationUtils.isViewHiddenByScale(authorImageView)) {

            ViewPropertyAnimator hideAuthorAnimator = com.rozdoum.socialcomponents.utils.AnimationUtils.hideViewByScale(authorImageView);
            hideAuthorAnimator.setListener(authorAnimatorListener);
            hideAuthorAnimator.withEndAction(PostDetailsActivity.this::onBackPressed);
        } else {
            super.onBackPressed();
        }
    }

    private void initListeners() {
        postImageView.setOnClickListener(v -> presenter.onPostImageClick());

        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sendButton.setEnabled(charSequence.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendButton.setOnClickListener(v -> presenter.onSendButtonClick());

        commentsCountTextView.setOnClickListener(view -> scrollToFirstComment());

        authorImageView.setOnClickListener(v -> presenter.onAuthorClick(v));
        authorTextView.setOnClickListener(v -> presenter.onAuthorClick(v));

        likesContainer.setOnClickListener(v -> {
            if (likeController != null && presenter.isPostExist()) {
                likeController.handleLikeClickAction(this, presenter.getPost());
            }
        });

        //long click for changing animation
        likesContainer.setOnLongClickListener(v -> {
            if (likeController != null) {
                likeController.changeAnimationType();
                return true;
            }

            return false;
        });
    }

    private void initRecyclerView() {
        commentsAdapter = new CommentsAdapter();
        commentsAdapter.setCallback(new CommentsAdapter.Callback() {
            @Override
            public void onLongItemClick(View view, int position) {
                Comment selectedComment = commentsAdapter.getItemByPosition(position);
                startActionMode(selectedComment);
            }

            @Override
            public void onAuthorClick(String authorId, View view) {
                openProfileActivity(authorId, view);
            }
        });
        commentsRecyclerView.setAdapter(commentsAdapter);
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.addItemDecoration(new DividerItemDecoration(commentsRecyclerView.getContext(),
                ((LinearLayoutManager) commentsRecyclerView.getLayoutManager()).getOrientation()));

        presenter.getCommentsList(this, postId);
    }

    private void startActionMode(Comment selectedComment) {
        if (mActionMode != null) {
            return;
        }

        //check access to modify or remove post
        if (presenter.hasAccessToEditComment(selectedComment.getAuthorId()) || presenter.hasAccessToModifyPost()) {
            mActionMode = startSupportActionMode(new ActionModeCallback(selectedComment));
        }
    }

    private void incrementWatchersCount() {
        postManager.incrementWatchersCount(postId);
        Intent intent = getIntent();
        setResult(RESULT_OK, intent.putExtra(POST_STATUS_EXTRA_KEY, PostStatus.UPDATED));
    }

    @Override
    public void scrollToFirstComment() {
        scrollView.smoothScrollTo(0, commentsLabel.getTop());
    }

    @Override
    public void clearCommentField() {
        commentEditText.setText(null);
        commentEditText.clearFocus();
        hideKeyboard();
    }

    private void scheduleStartPostponedTransition(final ImageView imageView) {
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public void openImageDetailScreen(String imagePath) {
        Intent intent = new Intent(this, ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_URL_EXTRA_KEY, imagePath);
        startActivity(intent);
    }

    @Override
    public void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(PostDetailsActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(PostDetailsActivity.this,
                            new android.util.Pair<>(view, getString(R.string.post_author_image_transition_name)));
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    @Override
    public void setDescription(String description) {
        descriptionEditText.setText(description);
    }

    @Override
    public void loadPostDetailImage(String imagePath) {
        int width = Utils.getDisplayWidth(this);
        int height = (int) getResources().getDimension(R.dimen.post_detail_image_height);

        ImageUtil.loadImageCenterCrop(GlideApp.with(this), imagePath, postImageView, width, height, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                scheduleStartPostponedTransition(postImageView);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                scheduleStartPostponedTransition(postImageView);
                progressBar.setVisibility(View.GONE);
                return false;
            }
        });
    }

    @Override
    public void loadAuthorPhoto(String photoUrl) {
        ImageUtil.loadImage(GlideApp.with(PostDetailsActivity.this), photoUrl, authorImageView, DiskCacheStrategy.DATA);
    }

    @Override
    public void setAuthorName(String username) {
        authorTextView.setText(username);
    }

    @Override
    public void initLikeController(@NonNull Post post) {
        likeController = new LikeController(this, post, likeCounterTextView, likesImageView, false);
    }

    @Override
    public void updateCounters(@NonNull Post post) {
        long commentsCount = post.getCommentsCount();
        commentsCountTextView.setText(String.valueOf(commentsCount));
        commentsLabel.setText(String.format(getString(R.string.label_comments), commentsCount));
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        likeController.setUpdatingLikeCounter(false);

        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(this, post.getCreatedDate());
        dateTextView.setText(date);

        presenter.updateCommentsVisibility(commentsCount);
    }

    @Override
    public void initLikeButtonState(boolean exist) {
        if (likeController != null) {
            likeController.initLike(exist);
        }
    }

    @Override
    public void showComplainMenuAction(boolean show) {
        if (complainActionMenuItem != null) {
            complainActionMenuItem.setVisible(show);
        }
    }

    @Override
    public void showEditMenuAction(boolean show) {
        if (editActionMenuItem != null) {
            editActionMenuItem.setVisible(show);
        }
    }

    @Override
    public void showDeleteMenuAction(boolean show) {
        if (deleteActionMenuItem != null) {
            deleteActionMenuItem.setVisible(show);
        }
    }

    @Override
    public String getCommentText() {
        return commentEditText.getText().toString();
    }

    @Override
    public void openEditPostActivity(Post post) {
        Intent intent = new Intent(PostDetailsActivity.this, EditPostActivity.class);
        intent.putExtra(EditPostActivity.POST_EXTRA_KEY, post);
        startActivityForResult(intent, EditPostActivity.EDIT_POST_REQUEST);
    }

    @Override
    public void showCommentProgress(boolean show) {
        commentsProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showCommentsWarning(boolean show) {
        warningCommentsTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showCommentsRecyclerView(boolean show) {
        commentsRecyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCommentsListChanged(List<Comment> list) {
        commentsAdapter.setList(list);
    }

    @Override
    public void showCommentsLabel(boolean show) {
        commentsLabel.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void openEditCommentDialog(Comment comment) {
        EditCommentDialog editCommentDialog = new EditCommentDialog();
        Bundle args = new Bundle();
        args.putString(EditCommentDialog.COMMENT_TEXT_KEY, comment.getText());
        args.putString(EditCommentDialog.COMMENT_ID_KEY, comment.getId());
        editCommentDialog.setArguments(args);
        editCommentDialog.show(getFragmentManager(), EditCommentDialog.TAG);
    }

    @Override
    public void onCommentChanged(String newText, String commentId) {
        presenter.updateComment(newText, commentId);
    }

    @Override
    public void onPostRemoved() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent.putExtra(POST_STATUS_EXTRA_KEY, PostStatus.REMOVED));
    }

    private class ActionModeCallback implements ActionMode.Callback {

        Comment selectedComment;

        ActionModeCallback(Comment selectedComment) {
            this.selectedComment = selectedComment;
        }

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.comment_context_menu, menu);

            menu.findItem(R.id.editMenuItem).setVisible(presenter.hasAccessToEditComment(selectedComment.getAuthorId()));

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }
        // Called when the user selects a contextual menu item

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.editMenuItem:
                    openEditCommentDialog(selectedComment);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.deleteMenuItem:
                    presenter.removeComment(selectedComment.getId());
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }
        // Called when the user exits the action mode

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }

    Animator.AnimatorListener authorAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            authorAnimationInProgress = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            authorAnimationInProgress = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            authorAnimationInProgress = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_details_menu, menu);
        complainActionMenuItem = menu.findItem(R.id.complain_action);
        editActionMenuItem = menu.findItem(R.id.edit_post_action);
        deleteActionMenuItem = menu.findItem(R.id.delete_post_action);
        presenter.updateOptionMenuVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!presenter.isPostExist()) {
            return super.onOptionsItemSelected(item);
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.complain_action:
                presenter.doComplainAction();
                return true;

            case R.id.edit_post_action:
                presenter.editPostAction();
                return true;

            case R.id.delete_post_action:
                presenter.attemptToRemovePost();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
