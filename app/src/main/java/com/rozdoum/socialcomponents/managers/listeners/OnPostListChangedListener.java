package com.rozdoum.socialcomponents.managers.listeners;

import com.rozdoum.socialcomponents.model.PostListResult;

public interface OnPostListChangedListener<Post> {

    public void onListChanged(PostListResult result);
}
