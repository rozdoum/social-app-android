/*
 * Copyright 2018 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rozdoum.socialcomponents.main.postDetails;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.main.base.BasePresenter;
import com.rozdoum.socialcomponents.main.base.BaseView;
import com.rozdoum.socialcomponents.managers.CommentManager;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.rozdoum.socialcomponents.managers.listeners.OnPostChangedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;

/**
 * Created by Alexey on 03.05.18.
 */

class PostDetailsPresenter extends BasePresenter<PostDetailsView> {
    private static final int TIME_OUT_LOADING_COMMENTS = 30000;

    private PostManager postManager;
    private ProfileManager profileManager;
    private CommentManager commentManager;
    private Post post;
    private boolean isPostExist;
    private boolean postRemovingProcess = false;

    private boolean attemptToLoadComments = false;

    PostDetailsPresenter(Activity activity) {
        super(activity);

        postManager = PostManager.getInstance(context.getApplicationContext());
        profileManager = ProfileManager.getInstance(context.getApplicationContext());
        commentManager = CommentManager.getInstance(context.getApplicationContext());
    }

    public void loadPost(String postId) {
        postManager.getPost(context, postId, new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                ifViewAttached(view -> {
                    if (obj != null) {
                        post = obj;
                        isPostExist = true;
                        view.initLikeController(post);
                        fillInUI(post);
                        view.updateCounters(post);
                        initLikeButtonState();
                        updateOptionMenuVisibility();
                    } else if (!postRemovingProcess) {
                        isPostExist = false;
                        view.onPostRemoved();
                        view.showNotCancelableWarningDialog(context.getString(R.string.error_post_was_removed));
                    }
                });
            }

            @Override
            public void onError(String errorText) {
                ifViewAttached(view -> {
                    view.showNotCancelableWarningDialog(errorText);
                });
            }
        });
    }

    private void initLikeButtonState() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && post != null) {
            postManager.hasCurrentUserLike(context, post.getId(), firebaseUser.getUid(), exist -> {
                ifViewAttached(view -> {
                    view.initLikeButtonState(exist);
                });
            });
        }
    }

    private void fillInUI(@NonNull Post post) {
        ifViewAttached(view -> {
            view.setTitle(post.getTitle());
            view.setDescription(post.getDescription());
            view.loadPostDetailImage(post.getImagePath());

            loadAuthorProfile();
        });
    }

    private void loadAuthorProfile() {
        if (post != null && post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), new OnObjectChangedListenerSimple<Profile>() {
                @Override
                public void onObjectChanged(Profile profile) {
                    ifViewAttached(view -> {
                        if (profile.getPhotoUrl() != null) {
                            view.loadAuthorPhoto(profile.getPhotoUrl());
                        }

                        view.setAuthorName(profile.getUsername());
                    });
                }
            });
        }
    }

    public void onAuthorClick(View authorView) {
        if (post != null) {
            ifViewAttached(view -> view.openProfileActivity(post.getAuthorId(), authorView));
        }
    }


    public void onSendButtonClick() {
        if (checkInternetConnection() && checkAuthorization()) {
            sendComment();
        }
    }

    public boolean hasAccessToModifyPost() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && post != null && post.getAuthorId().equals(currentUser.getUid());
    }

    public boolean hasAccessToEditComment(String commentAuthorId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && commentAuthorId.equals(currentUser.getUid());
    }

    public void updateComment(String newText, String commentId) {
        ifViewAttached(BaseView::showProgress);
        if (post != null) {
            commentManager.updateComment(commentId, newText, post.getId(), success -> {
                ifViewAttached(view -> {
                    view.hideProgress();
                    view.showSnackBar(R.string.message_comment_was_edited);
                });
            });
        }
    }

    private void openComplainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.add_complain)
                .setMessage(R.string.complain_text)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.add_complain, (dialogInterface, i) -> addComplain());

        builder.create().show();
    }

    private void addComplain() {
        postManager.addComplain(post);
        ifViewAttached(view -> {
            view.showComplainMenuAction(false);
            view.showSnackBar(R.string.complain_sent);
        });
    }

    public void doComplainAction() {
        if (checkAuthorization()) {
            openComplainDialog();
        }
    }

    public void attemptToRemovePost() {
        if (hasAccessToModifyPost() && checkInternetConnection()) {
            if (!postRemovingProcess) {
                openConfirmDeletingDialog();
            }
        }
    }

    private void openConfirmDeletingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.confirm_deletion_post)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_ok, (dialogInterface, i) -> removePost());

        builder.create().show();
    }

    private void removePost() {
        postRemovingProcess = true;
        ifViewAttached(view -> view.showProgress(R.string.removing));
        postManager.removePost(post, success -> ifViewAttached(view -> {
            if (success) {
                view.onPostRemoved();
                view.finish();
            } else {
                postRemovingProcess = false;
                view.showSnackBar(R.string.error_fail_remove_post);
            }

            view.hideProgress();
        }));


    }

    private void sendComment() {
        if (post == null) {
            return;
        }

        ifViewAttached(view -> {
            String commentText = view.getCommentText();

            if (commentText.length() > 0 && isPostExist) {
                createOrUpdateComment(commentText);
                view.clearCommentField();
            }
        });
    }

    private void createOrUpdateComment(String commentText) {
        commentManager.createOrUpdateComment(commentText, post.getId(), success -> {
            ifViewAttached(view -> {
                if (success) {
                    if (post != null && post.getCommentsCount() > 0) {
                        view.scrollToFirstComment();
                    }
                }
            });
        });
    }

    public void removeComment(String commentId) {
        ifViewAttached(BaseView::showProgress);
        commentManager.removeComment(commentId, post.getId(), success -> {
            ifViewAttached(view -> {
                view.hideProgress();
                view.showSnackBar(R.string.message_comment_was_removed);
            });
        });
    }

    public void editPostAction() {
        if (hasAccessToModifyPost() && checkInternetConnection()) {
            ifViewAttached(view -> view.openEditPostActivity(post));
        }
    }

    public void updateOptionMenuVisibility() {
        ifViewAttached(view -> {
            if (post != null) {
                view.showEditMenuAction(hasAccessToModifyPost());
                view.showDeleteMenuAction(hasAccessToModifyPost());
                view.showComplainMenuAction(!post.isHasComplain());
            }
        });
    }

    public boolean isPostExist() {
        return isPostExist;
    }

    public Post getPost() {
        return post;
    }

    public void onPostImageClick() {
        ifViewAttached(view -> {
            if (post != null && post.getImagePath() != null) {
                view.openImageDetailScreen(post.getImagePath());
            }
        });
    }

    public void getCommentsList(Context activityContext, String postId) {
        attemptToLoadComments = true;
        runHidingCommentProgressByTimeOut();

        commentManager.getCommentsList(activityContext, postId, list -> {
            attemptToLoadComments = false;
            ifViewAttached(view -> {
                view.onCommentsListChanged(list);
                view.showCommentProgress(false);
                view.showCommentsRecyclerView(true);
                view.showCommentsWarning(false);
            });
        });
    }

    private void runHidingCommentProgressByTimeOut() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (attemptToLoadComments) {
                ifViewAttached(view -> {
                    view.showCommentProgress(false);
                    view.showCommentsWarning(true);
                });
            }
        }, TIME_OUT_LOADING_COMMENTS);
    }

    public void updateCommentsVisibility(long commentsCount) {
        ifViewAttached(view -> {
            if (commentsCount == 0) {
                view.showCommentsLabel(false);
                view.showCommentProgress(false);
            } else {
                view.showCommentsLabel(true);
            }
        });
    }
}
