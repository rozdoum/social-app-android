package com.rozdoum.socialcomponents.managers;

import android.content.Context;
import android.util.Log;

import com.rozdoum.socialcomponents.ApplicationHelper;
import com.rozdoum.socialcomponents.model.Post;

import java.util.List;

/**
 * Created by Kristina on 10/28/16.
 */

public class PostManager {

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

    //TODO: in progress
//    public List<Post> getPosts() {
//
//    }
}
