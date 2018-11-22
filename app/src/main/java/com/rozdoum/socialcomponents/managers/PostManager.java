/*
 * Copyright 2017 Rozdoum
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

package com.rozdoum.socialcomponents.managers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.main.interactors.FollowInteractor;
import com.rozdoum.socialcomponents.main.interactors.PostInteractor;
import com.rozdoum.socialcomponents.managers.listeners.*;
import com.rozdoum.socialcomponents.model.FollowingPost;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.*;

/**
 * Created by Kristina on 10/28/16.
 */

public class PostManager extends FirebaseListenersManager {

    private static final String TAG = PostManager.class.getSimpleName();
    private static PostManager instance;
    private int newPostsCounter = 0;
    private PostCounterWatcher postCounterWatcher;
    private PostInteractor postInteractor;

    private Context context;

    public static PostManager getInstance(Context context) {
        if (instance == null) {
            instance = new PostManager(context);
        }

        return instance;
    }

    private PostManager(Context context) {
        this.context = context;
        postInteractor = PostInteractor.getInstance(context);
    }

    public void createOrUpdatePost(Post post) {
        try {
            postInteractor.createOrUpdatePost(post);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void getPostsList(OnPostListChangedListener<Post> onDataChangedListener, long date) {
        postInteractor.getPostList(onDataChangedListener, date);
    }

    public void getPostsListByUser(OnDataChangedListener<Post> onDataChangedListener, String userId) {
        postInteractor.getPostListByUser(onDataChangedListener, userId);
    }

    public void getPost(Context context, String postId, OnPostChangedListener onPostChangedListener) {
        ValueEventListener valueEventListener = postInteractor.getPost(postId, onPostChangedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void getSinglePostValue(String postId, OnPostChangedListener onPostChangedListener) {
        postInteractor.getSinglePost(postId, onPostChangedListener);
    }

    public void createOrUpdatePostWithImage(Uri imageUri, final OnPostCreatedListener onPostCreatedListener, final Post post) {
        postInteractor.createOrUpdatePostWithImage(imageUri, onPostCreatedListener, post);
    }

    public void removePost(final Post post, final OnTaskCompleteListener onTaskCompleteListener) {
        postInteractor.removePost(post, onTaskCompleteListener);
    }

    public void addComplain(Post post) {
        postInteractor.addComplainToPost(post);
    }

    public void hasCurrentUserLike(Context activityContext, String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        ValueEventListener valueEventListener = postInteractor.hasCurrentUserLike(postId, userId, onObjectExistListener);
        addListenerToMap(activityContext, valueEventListener);
    }

    public void hasCurrentUserLikeSingleValue(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        postInteractor.hasCurrentUserLikeSingleValue(postId, userId, onObjectExistListener);
    }

    public void isPostExistSingleValue(String postId, final OnObjectExistListener<Post> onObjectExistListener) {
        postInteractor.isPostExistSingleValue(postId, onObjectExistListener);
    }

    public void incrementWatchersCount(String postId) {
        postInteractor.incrementWatchersCount(postId);
    }

    public void incrementNewPostsCounter() {
        newPostsCounter++;
        notifyPostCounterWatcher();
    }

    public void clearNewPostsCounter() {
        newPostsCounter = 0;
        notifyPostCounterWatcher();
    }

    public int getNewPostsCounter() {
        return newPostsCounter;
    }

    public void setPostCounterWatcher(PostCounterWatcher postCounterWatcher) {
        this.postCounterWatcher = postCounterWatcher;
    }

    private void notifyPostCounterWatcher() {
        if (postCounterWatcher != null) {
            postCounterWatcher.onPostCounterChanged(newPostsCounter);
        }
    }

    public void getFollowingPosts(String userId, OnDataChangedListener<FollowingPost> listener) {
        FollowInteractor.getInstance(context).getFollowingPosts(userId, listener);
    }

    public void searchByTitle(String searchText, OnDataChangedListener<Post> onDataChangedListener) {
        closeListeners(context);
        ValueEventListener valueEventListener = postInteractor.searchPostsByTitle(searchText, onDataChangedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void filterByLikes(int limit, OnDataChangedListener<Post> onDataChangedListener) {
        closeListeners(context);
        ValueEventListener valueEventListener = postInteractor.filterPostsByLikes(limit, onDataChangedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void loadImageMediumSize(GlideRequests request, String imageTitle, ImageView imageView, @Nullable OnImageRequestListener onImageRequestListener) {
        int width = Utils.getDisplayWidth(context);
        int height = (int) context.getResources().getDimension(R.dimen.post_detail_image_height);

        StorageReference mediumStorageRef = getMediumImageStorageRef(imageTitle);
        StorageReference originalStorageRef = getOriginImageStorageRef(imageTitle);

        ImageUtil.loadMediumImageCenterCrop(request, mediumStorageRef, originalStorageRef, imageView, width, height, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (onImageRequestListener != null) {
                    onImageRequestListener.onImageRequestFinished();
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                if (onImageRequestListener != null) {
                    onImageRequestListener.onImageRequestFinished();
                }
                return false;
            }
        });

    }

    public void loadImageMediumSize(GlideRequests request, String imageTitle, ImageView imageView) {
        loadImageMediumSize(request, imageTitle, imageView, null);
    }

    private StorageReference getMediumImageStorageRef(String imageTitle) {
        return postInteractor.getMediumImageStorageRef(imageTitle);
    }

    public StorageReference getSmallImageStorageRef(String imageTitle) {
        return postInteractor.getSmallImageStorageRef(imageTitle);
    }

    public StorageReference getOriginImageStorageRef(String imageTitle) {
        return postInteractor.getOriginImageStorageRef(imageTitle);
    }

    public interface PostCounterWatcher {
        void onPostCounterChanged(int newValue);
    }

    public interface OnImageRequestListener {
        void onImageRequestFinished();
    }
}
