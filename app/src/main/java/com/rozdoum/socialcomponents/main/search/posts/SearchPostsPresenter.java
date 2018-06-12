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


package com.rozdoum.socialcomponents.main.search.posts;

import android.content.Context;

import com.rozdoum.socialcomponents.main.base.BasePresenter;
import com.rozdoum.socialcomponents.main.interactors.PostInteractor;
import com.rozdoum.socialcomponents.utils.LogUtil;

/**
 * Created by Alexey on 08.06.18.
 */
public class SearchPostsPresenter extends BasePresenter<SearchPostsView> {
    private Context context;
    private PostInteractor postInteractor;

    public SearchPostsPresenter(Context context) {
        super(context);
        this.context = context;
        postInteractor = PostInteractor.getInstance(context.getApplicationContext());
    }

    public void search(String searchText) {
        if (checkInternetConnection()) {
            ifViewAttached(SearchPostsView::showLocalProgress);
            postInteractor.searchPosts(searchText, list -> {
                ifViewAttached(view -> {
                    view.hideLocalProgress();
                    view.onSearchResultsReady(list);

                    if (list.isEmpty()) {
                        view.showEmptyListLayout();
                    }
                });

                LogUtil.logDebug(TAG, "search text: " + searchText);
                LogUtil.logDebug(TAG, "found items count: " + list.size());
            });
        } else {
            ifViewAttached(SearchPostsView::hideLocalProgress);
        }
    }

    public void loadPostsWithEmptySearch() {
        search("");
    }
}
