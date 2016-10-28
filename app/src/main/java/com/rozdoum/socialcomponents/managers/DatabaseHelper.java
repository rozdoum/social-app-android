package com.rozdoum.socialcomponents.managers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rozdoum.socialcomponents.model.Post;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kristina on 10/28/16.
 */

public class DatabaseHelper {

    public static final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper instance;

    private Context context;
    private FirebaseDatabase database;

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
    }

    public void createOrUpdatePost(Post post) {
        try {
            DatabaseReference databaseReference = database.getReference("posts");
            String placeHistoryId = databaseReference.child("post").push().getKey();
            Map<String, Object> messageValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/post/" + placeHistoryId, messageValues);

            databaseReference.updateChildren(childUpdates);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
