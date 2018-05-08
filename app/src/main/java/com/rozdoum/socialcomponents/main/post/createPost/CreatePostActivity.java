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

package com.rozdoum.socialcomponents.main.post.createPost;

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.main.post.BaseCreatePostActivity;
import com.rozdoum.socialcomponents.model.Post;

public class CreatePostActivity extends BaseCreatePostActivity<CreatePostView, CreatePostPresenter> implements CreatePostView {
    public static final int CREATE_NEW_POST_REQUEST = 11;

    @NonNull
    @Override
    public CreatePostPresenter createPresenter() {
        if (presenter == null) {
            return new CreatePostPresenter(this);
        }
        return presenter;
    }

    @Override
    protected int getSaveFailMessage() {
        return R.string.error_fail_create_post;
    }

    @Override
    protected void savePost(String title, String description) {
        showProgress(R.string.message_creating_post);
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setAuthorId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        postManager.createOrUpdatePostWithImage(imageUri, this, post);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.post:
                doSavePost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
