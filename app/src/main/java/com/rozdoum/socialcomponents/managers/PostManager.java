package com.rozdoum.socialcomponents.managers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.LogUtil;

/**
 * Created by Kristina on 10/28/16.
 */

public class PostManager extends FirebaseListenersManager {

    private static final String TAG = PostManager.class.getSimpleName();
    private static PostManager instance;

    private Context context;

    public static PostManager getInstance(Context context) {
        if (instance == null) {
            instance = new PostManager(context);
        }

        return instance;
    }

    private PostManager(Context context) {
        this.context = context;
    }

    public void createOrUpdatePost(Post post) {
        try {
            ApplicationHelper.getDatabaseHelper().createOrUpdatePost(post);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void getPostsList(OnDataChangedListener<Post> onDataChangedListener, long date) {
        ApplicationHelper.getDatabaseHelper().getPostList(onDataChangedListener, date);
    }

    public void getPostsListByUser(OnDataChangedListener<Post> onDataChangedListener, String userId) {
        ApplicationHelper.getDatabaseHelper().getPostListByUser(onDataChangedListener, userId);
    }

    public void getPost(Context context, String postId, OnObjectChangedListener<Post> onObjectChangedListener) {
        ValueEventListener valueEventListener = ApplicationHelper.getDatabaseHelper().getPost(postId, onObjectChangedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void getSinglePostValue(String postId, OnObjectChangedListener<Post> onObjectChangedListener) {
        ApplicationHelper.getDatabaseHelper().getSinglePost(postId, onObjectChangedListener);
    }

    public void getCommentsList(Context context, String postId, OnDataChangedListener<Comment> onDataChangedListener) {
        ValueEventListener valueEventListener = ApplicationHelper.getDatabaseHelper().getCommentsList(postId, onDataChangedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void createPostWithImage(Uri imageUri, final OnPostCreatedListener onPostCreatedListener, final Post post) {
        // Register observers to listen for when the download is done or if it fails
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        UploadTask uploadTask = databaseHelper.uploadImage(imageUri);

        if (uploadTask != null) {
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    onPostCreatedListener.onPostCreated(false);

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    LogUtil.logDebug(TAG, "successful upload image, image url: " + String.valueOf(downloadUrl));

                    post.setImagePath(String.valueOf(downloadUrl));
                    createOrUpdatePost(post);

                    onPostCreatedListener.onPostCreated(true);
                }
            });
        }
    }

    public void addComplain(Post post) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        databaseHelper.addComplainToPost(post);
    }

    public void hasCurrentUserLike(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        ValueEventListener valueEventListener = databaseHelper.hasCurrentUserLike(postId, userId, onObjectExistListener);
        addListenerToMap(context, valueEventListener);
    }

    public void hasCurrentUserLikeSingleValue(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        databaseHelper.hasCurrentUserLikeSingleValue(postId, userId, onObjectExistListener);
    }
}
