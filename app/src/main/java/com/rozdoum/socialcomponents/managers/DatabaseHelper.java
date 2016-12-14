package com.rozdoum.socialcomponents.managers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectExistListener;
import com.rozdoum.socialcomponents.model.Comment;
import com.rozdoum.socialcomponents.model.Like;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kristina on 10/28/16.
 */

public class DatabaseHelper {

    public static final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper instance;
    private static final int POST_AMOUNT_ON_PAGE = 10;

    private Context context;
    private FirebaseDatabase database;
    FirebaseStorage storage;

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }

        return instance;
    }

    public DatabaseHelper(Context context) {
        this.context = context;
    }

    public void init() {
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        storage = FirebaseStorage.getInstance();
    }

    public void createOrUpdatePost(Post post) {
        try {
            DatabaseReference databaseReference = database.getReference();
            String postId = databaseReference.child("posts").push().getKey();
            post.setId(postId);
            Map<String, Object> postValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/posts/" + postId, postValues);

            databaseReference.updateChildren(childUpdates);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void createOrUpdateComment(String commentText, final String postId) {
        try {
            DatabaseReference mCommentsReference = database.getReference().child("post-comments/" + postId);
            String commentId = mCommentsReference.push().getKey();
            Comment comment = new Comment(commentText);
            comment.setId(commentId);

            mCommentsReference.child(commentId).setValue(comment, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        incrementCommentsCount(postId);
                    } else {
                        LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                    }
                }

                private void incrementCommentsCount(String postId) {
                    DatabaseReference postRef = database.getReference("posts/" + postId + "/commentsCount");
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
                            LogUtil.logInfo(TAG, "Updating comments count transaction is completed.");
                        }
                    });
                }
            });
        } catch (Exception e) {
            LogUtil.logError(TAG, "createOrUpdateComment()", e);
        }
    }

    public void createOrUpdateLike(final String postId, String authorId) {
        try {
            DatabaseReference mLikesReference = database.getReference().child("post-likes").child(postId).child(authorId);
            mLikesReference.push();
            String id = mLikesReference.push().getKey();
            Like like = new Like(authorId);
            like.setId(id);

            mLikesReference.child(id).setValue(like, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        incrementLikesCount(postId);
                    } else {
                        LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                    }
                }

                private void incrementLikesCount(String postId) {
                    DatabaseReference postRef = database.getReference("posts/" + postId + "/likesCount");
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

    public void removeLike(final String postId, String authorId) {
        DatabaseReference mLikesReference = database.getReference().child("post-likes").child(postId).child(authorId);
        mLikesReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    decrementLikesCount(postId);
                } else {
                    LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                }
            }

            private void decrementLikesCount(String postId) {
                DatabaseReference postRef = database.getReference("posts/" + postId + "/likesCount");
                postRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
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

    public UploadTask uploadImage(Uri uri) {
        StorageReference storageRef = storage.getReferenceFromUrl("gs://socialcomponents.appspot.com");
        StorageReference riversRef = storageRef.child("images/" + uri.getLastPathSegment());
        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCacheControl("max-age=7776000, Expires=7776000, public, must-revalidate")
                .build();

        return riversRef.putFile(uri, metadata);
    }

    public void getPostList(final OnDataChangedListener<Post> onDataChangedListener, long date) {
        DatabaseReference databaseReference = database.getReference("posts");
        Query postsQuery;
        if (date == 0) {
            postsQuery = databaseReference.limitToLast(POST_AMOUNT_ON_PAGE).orderByChild("createdDate");
        } else {
            postsQuery = databaseReference.limitToLast(POST_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdDate");
        }

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onDataChangedListener.onListChanged(parsePostList(dataSnapshot));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private List<Post> parsePostList(DataSnapshot dataSnapshot) {
        Map<String, Object> objectMap = (HashMap<String, Object>)
                dataSnapshot.getValue();
        List<Post> list = new ArrayList<Post>();
        if (objectMap != null) {
            for (String key : objectMap.keySet()) {
                Object obj = objectMap.get(key);
                if (obj instanceof Map) {
                    Map<String, Object> mapObj = (Map<String, Object>) obj;
                    Post post = new Post();
                    post.setId(key);
                    post.setTitle((String) mapObj.get("title"));
                    post.setDescription((String) mapObj.get("description"));
                    post.setImagePath((String) mapObj.get("imagePath"));
                    post.setCreatedDate((long) mapObj.get("createdDate"));
                    if (mapObj.containsValue("commentsCount")) {
                        post.setCommentsCount((int) mapObj.get("commentsCount"));
                    }
                    if (mapObj.containsValue("likesCount")) {
                        post.setLikesCount((int) mapObj.get("likesCount"));
                    }
                    list.add(post);
                }
            }

            Collections.sort(list, new Comparator<Post>() {
                @Override
                public int compare(Post lhs, Post rhs) {
                    return ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate());
                }
            });
        }

        return list;
    }

    public void getCommentsList(String postId, final OnDataChangedListener<Comment> onDataChangedListener) {
        DatabaseReference databaseReference = database.getReference("post-comments").child(postId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Comment> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    list.add(comment);
                }

                Collections.sort(list, new Comparator<Comment>() {
                    @Override
                    public int compare(Comment lhs, Comment rhs) {
                        return ((Long) rhs.getCreatedDate()).compareTo((Long) lhs.getCreatedDate());
                    }
                });

                onDataChangedListener.onListChanged(list);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void hasCurrentUserLike(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference = database.getReference("post-likes").child(postId).child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
