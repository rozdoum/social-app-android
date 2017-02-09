package com.rozdoum.socialcomponents.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnPostChangedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.ImageUtil;

public class EditPostActivity extends CreatePostActivity {
    private static final String TAG = EditPostActivity.class.getSimpleName();
    public static final String POST_EXTRA_KEY = "EditPostActivity.POST_EXTRA_KEY";
    public static final int EDIT_POST_REQUEST = 33;

    private ImageUtil imageUtil;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageUtil = ImageUtil.getInstance(this);
        post = (Post) getIntent().getSerializableExtra(POST_EXTRA_KEY);
        showProgress();
        fillUIFields();
    }

    @Override
    protected void onStart() {
        super.onStart();
        addCheckIsPostChangedListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        postManager.closeListeners(this);
    }

    @Override
    public void onPostSaved(boolean success) {
        hideProgress();
        creatingPost = false;

        if (success) {
            setResult(RESULT_OK);
            finish();
        } else {
            showSnackBar(R.string.error_fail_update_post);
        }
    }

    @Override
    protected void savePost(final String title, final String description) {
        doSavePost(title, description);
    }

    private void addCheckIsPostChangedListener() {
        PostManager.getInstance(this).getPost(this, post.getId(), new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                if (obj == null) {
                    showWarningDialog(getResources().getString(R.string.error_post_was_removed));
                } else {
                    checkIsPostCountersChanged(obj);
                }
            }

            @Override
            public void onError(String errorText) {
                showWarningDialog(errorText);
            }

            private void showWarningDialog(String message) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openMainActivity();
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        });
    }

    private void checkIsPostCountersChanged(Post updatedPost) {
        if (post.getLikesCount() != updatedPost.getLikesCount()) {
            post.setLikesCount(updatedPost.getLikesCount());
        }

        if (post.getCommentsCount() != updatedPost.getCommentsCount()) {
            post.setCommentsCount(updatedPost.getCommentsCount());
        }

        if (post.isHasComplain() != updatedPost.isHasComplain()) {
            post.setHasComplain(updatedPost.isHasComplain());
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(EditPostActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void doSavePost(String title, String description) {
        showProgress(R.string.message_saving);
        post.setTitle(title);
        post.setDescription(description);

        if (imageUri != null) {
            postManager.createOrUpdatePostWithImage(imageUri, EditPostActivity.this, post);
        } else {
            postManager.createOrUpdatePost(post);
            onPostSaved(true);
        }
    }

    private void fillUIFields() {
        titleEditText.setText(post.getTitle());
        descriptionEditText.setText(post.getDescription());
        loadPostDetailsImage();
        hideProgress();
    }

    private void loadPostDetailsImage() {
        String imageUrl = post.getImagePath();
        imageUtil.getFullImage(imageUrl, imageView, R.drawable.ic_stub);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:
                if (!creatingPost) {
                    if (hasInternetConnection()) {
                        attemptCreatePost();
                    } else {
                        showSnackBar(R.string.internet_connection_failed);
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
