package com.rozdoum.socialcomponents.managers.listeners;

import com.rozdoum.socialcomponents.model.Post;

public interface OnPostChangedListener {
    public void onObjectChanged(Post obj);

    public void onError(String errorText);
}
