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

package com.rozdoum.socialcomponents.main.main;

import android.view.View;

import com.rozdoum.socialcomponents.main.base.BaseView;
import com.rozdoum.socialcomponents.model.Post;

/**
 * Created by Alexey on 03.05.18.
 */

public interface MainView extends BaseView {
    void openCreatePostActivity();
    void hideCounterView();
    void openPostDetailsActivity(Post post, View v);
    void showFloatButtonRelatedSnackBar(int messageId);
    void openProfileActivity(String userId, View view);
    void refreshPostList();
    void removePost();
    void updatePost();
    void showCounterView(int count);
}
