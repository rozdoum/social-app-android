/*
 *
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
 *
 */

package com.rozdoum.socialcomponents.main.interactors;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.UploadTask;
import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.Constants;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.enums.UploadImagePrefix;
import com.rozdoum.socialcomponents.managers.DatabaseHelper;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.managers.listeners.OnPostChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnPostListChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnTaskCompleteListener;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.PostListResult;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alexey on 05.06.18.
 */

public class PostInteractor {

    private static final String TAG = PostInteractor.class.getSimpleName();
    private static PostInteractor instance;

    private DatabaseHelper databaseHelper;
    private Context context;

    public static PostInteractor getInstance(Context context) {
        if (instance == null) {
            instance = new PostInteractor(context);
        }

        return instance;
    }

    private PostInteractor(Context context) {
        this.context = context;
        databaseHelper = ApplicationHelper.getDatabaseHelper();
    }

    public String generatePostId() {
        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POSTS_DB_KEY)
                .push()
                .getKey();
    }

    public void createOrUpdatePost(Post post) {
        try {
            Map<String, Object> postValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + DatabaseHelper.POSTS_DB_KEY + "/" + post.getId(), postValues);

            databaseHelper.getDatabaseReference().updateChildren(childUpdates);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public Task<Void> removePost(Post post) {
        DatabaseReference postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(post.getId());
        return postRef.removeValue();
    }

    public void incrementWatchersCount(String postId) {
        DatabaseReference postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/watchersCount");
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating Watchers count transaction is completed.");
            }
        });
    }

    public void getPostList(final OnPostListChangedListener<Post> onDataChangedListener, long date) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        Query postsQuery;
        if (date == 0) {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).orderByChild("createdDate");
        } else {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdDate");
        }

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
                PostListResult result = parsePostList(objectMap);

                if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                    getPostList(onDataChangedListener, result.getLastItemCreatedDate() - 1);
                } else {
                    onDataChangedListener.onListChanged(parsePostList(objectMap));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostList(), onCancelled", new Exception(databaseError.getMessage()));
                onDataChangedListener.onCanceled(context.getString(R.string.permission_denied_error));
            }
        });
    }

    public void getPostListByUser(final OnDataChangedListener<Post> onDataChangedListener, String userId) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        Query postsQuery;
        postsQuery = databaseReference.orderByChild("authorId").equalTo(userId);

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostListByUser(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public ValueEventListener getPost(final String id, final OnPostChangedListener listener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setId(id);
                        }
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onObjectChanged(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void getSinglePost(final String id, final OnPostChangedListener listener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.exists()) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        post.setId(id);
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onError(context.getString(R.string.message_post_was_removed));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getSinglePost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    private PostListResult parsePostList(Map<String, Object> objectMap) {
        PostListResult result = new PostListResult();
        List<Post> list = new ArrayList<Post>();
        boolean isMoreDataAvailable = true;
        long lastItemCreatedDate = 0;

        if (objectMap != null) {
            isMoreDataAvailable = Constants.Post.POST_AMOUNT_ON_PAGE == objectMap.size();

            for (String key : objectMap.keySet()) {
                Object obj = objectMap.get(key);
                if (obj instanceof Map) {
                    Map<String, Object> mapObj = (Map<String, Object>) obj;

                    if (!isPostValid(mapObj)) {
                        LogUtil.logDebug(TAG, "Invalid post, id: " + key);
                        continue;
                    }

                    boolean hasComplain = mapObj.containsKey("hasComplain") && (boolean) mapObj.get("hasComplain");
                    long createdDate = (long) mapObj.get("createdDate");

                    if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                        lastItemCreatedDate = createdDate;
                    }

                    if (!hasComplain) {
                        Post post = new Post();
                        post.setId(key);
                        post.setTitle((String) mapObj.get("title"));
                        post.setDescription((String) mapObj.get("description"));
                        post.setImagePath((String) mapObj.get("imagePath"));
                        post.setImageTitle((String) mapObj.get("imageTitle"));
                        post.setAuthorId((String) mapObj.get("authorId"));
                        post.setCreatedDate(createdDate);
                        if (mapObj.containsKey("commentsCount")) {
                            post.setCommentsCount((long) mapObj.get("commentsCount"));
                        }
                        if (mapObj.containsKey("likesCount")) {
                            post.setLikesCount((long) mapObj.get("likesCount"));
                        }
                        if (mapObj.containsKey("watchersCount")) {
                            post.setWatchersCount((long) mapObj.get("watchersCount"));
                        }
                        list.add(post);
                    }
                }
            }

            Collections.sort(list, (lhs, rhs) -> ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate()));

            result.setPosts(list);
            result.setLastItemCreatedDate(lastItemCreatedDate);
            result.setMoreDataAvailable(isMoreDataAvailable);
        }

        return result;
    }

    private boolean isPostValid(Map<String, Object> post) {
        return post.containsKey("title")
                && post.containsKey("description")
                && post.containsKey("imagePath")
                && post.containsKey("imageTitle")
                && post.containsKey("authorId")
                && post.containsKey("description");
    }

    public void addComplainToPost(Post post) {
        databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(post.getId()).child("hasComplain").setValue(true);
    }

    public void isPostExistSingleValue(String postId, final OnObjectExistListener<Post> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(postId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "isPostExistSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void subscribeToNewPosts() {
        FirebaseMessaging.getInstance().subscribeToTopic("postsTopic");
    }

    public void removePost(final Post post, final OnTaskCompleteListener onTaskCompleteListener) {
        final DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        Task<Void> removeImageTask = databaseHelper.removeImage(post.getImageTitle());

        removeImageTask.addOnSuccessListener(aVoid -> {
            removePost(post).addOnCompleteListener(task -> {
                onTaskCompleteListener.onTaskComplete(task.isSuccessful());
                ProfileInteractor.getInstance(context).updateProfileLikeCountAfterRemovingPost(post);
                removeObjectsRelatedToPost(post.getId());
                LogUtil.logDebug(TAG, "removePost(), is success: " + task.isSuccessful());
            });
            LogUtil.logDebug(TAG, "removeImage(): success");
        }).addOnFailureListener(exception -> {
            LogUtil.logError(TAG, "removeImage()", exception);
            onTaskCompleteListener.onTaskComplete(false);
        });
    }

    private void removeObjectsRelatedToPost(final String postId) {
        CommentInteractor.getInstance(context).removeCommentsByPost(postId).addOnSuccessListener(aVoid -> LogUtil.logDebug(TAG, "Comments related to post with id: " + postId + " was removed")).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LogUtil.logError(TAG, "Failed to remove comments related to post with id: " + postId, e);
            }
        });

        removeLikesByPost(postId).addOnSuccessListener(aVoid -> LogUtil.logDebug(TAG, "Likes related to post with id: " + postId + " was removed")).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LogUtil.logError(TAG, "Failed to remove likes related to post with id: " + postId, e);
            }
        });
    }

    public void createOrUpdatePostWithImage(Uri imageUri, final OnPostCreatedListener onPostCreatedListener, final Post post) {
        // Register observers to listen for when the download is done or if it fails
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        if (post.getId() == null) {
            post.setId(generatePostId());
        }

        final String imageTitle = ImageUtil.generateImageTitle(UploadImagePrefix.POST, post.getId());
        UploadTask uploadTask = databaseHelper.uploadImage(imageUri, imageTitle);

        if (uploadTask != null) {
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                onPostCreatedListener.onPostSaved(false);

            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                LogUtil.logDebug(TAG, "successful upload image, image url: " + String.valueOf(downloadUrl));

                post.setImagePath(String.valueOf(downloadUrl));
                post.setImageTitle(imageTitle);
                createOrUpdatePost(post);

                onPostCreatedListener.onPostSaved(true);
            });
        }
    }

    public void createOrUpdateLike(final String postId, final String postAuthorId) {
        try {
            String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mLikesReference = databaseHelper
                    .getDatabaseReference()
                    .child(DatabaseHelper.POST_LIKES_DB_KEY)
                    .child(postId)
                    .child(authorId);
            mLikesReference.push();
            String id = mLikesReference.push().getKey();
            Like like = new Like(authorId);
            like.setId(id);

            mLikesReference.child(id).setValue(like, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        DatabaseReference postRef = databaseHelper
                                .getDatabaseReference()
                                .child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/likesCount");

                        incrementLikesCount(postRef);
                        DatabaseReference profileRef = databaseHelper
                                .getDatabaseReference()
                                .child(DatabaseHelper.PROFILES_DB_KEY + "/" + postAuthorId + "/likesCount");

                        incrementLikesCount(profileRef);
                    } else {
                        LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                    }
                }

                private void incrementLikesCount(DatabaseReference postRef) {
                    postRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentValue = mutableData.getValue(Integer.class);
                            if (currentValue == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue(currentValue + 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                        }
                    });
                }

            });
        } catch (Exception e) {
            LogUtil.logError(TAG, "createOrUpdateLike()", e);
        }

    }

    public void removeLike(final String postId, final String postAuthorId) {
        String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mLikesReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .child(authorId);
        mLikesReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    DatabaseReference postRef = databaseHelper
                            .getDatabaseReference()
                            .child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/likesCount");
                    decrementLikesCount(postRef);

                    DatabaseReference profileRef = databaseHelper
                            .getDatabaseReference()
                            .child(DatabaseHelper.PROFILES_DB_KEY + "/" + postAuthorId + "/likesCount");
                    decrementLikesCount(profileRef);
                } else {
                    LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                }
            }

            private void decrementLikesCount(DatabaseReference postRef) {
                postRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Long currentValue = mutableData.getValue(Long.class);
                        if (currentValue == null) {
                            mutableData.setValue(0);
                        } else {
                            mutableData.setValue(currentValue - 1);
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                    }
                });
            }
        });
    }

    public ValueEventListener hasCurrentUserLike(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .child(userId);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLike(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void hasCurrentUserLikeSingleValue(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .child(userId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLikeSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public Task<Void> removeLikesByPost(String postId) {
        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .removeValue();
    }

    public ValueEventListener searchPostsByTitle(String searchText, OnDataChangedListener<Post> onDataChangedListener) {
        DatabaseReference reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        ValueEventListener valueEventListener = getSearchQuery(reference,"title", searchText).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "searchPostsByTitle(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, reference);

        return valueEventListener;
    }

    public ValueEventListener filterPostsByLikes(int  limit, OnDataChangedListener<Post> onDataChangedListener) {
        DatabaseReference reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        ValueEventListener valueEventListener = getFilteredQuery(reference,"likesCount", limit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "filterPostsByLikes(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, reference);

        return valueEventListener;
    }

    private Query getSearchQuery(DatabaseReference databaseReference, String childOrderBy, String searchText) {
        return databaseReference
                .orderByChild(childOrderBy)
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");
    }

    private Query getFilteredQuery(DatabaseReference databaseReference, String childOrderBy, int limit) {
        return databaseReference
                .orderByChild(childOrderBy)
                .limitToLast(limit);
    }
}
