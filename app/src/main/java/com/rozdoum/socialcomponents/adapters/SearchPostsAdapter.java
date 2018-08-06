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

package com.rozdoum.socialcomponents.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.holders.PostViewHolder;
import com.rozdoum.socialcomponents.controllers.LikeController;
import com.rozdoum.socialcomponents.main.base.BaseActivity;
import com.rozdoum.socialcomponents.model.Post;

import java.util.List;


public class SearchPostsAdapter extends BasePostsAdapter {
    public static final String TAG = SearchPostsAdapter.class.getSimpleName();

    private CallBack callBack;

    public SearchPostsAdapter(final BaseActivity activity) {
        super(activity);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.post_item_list_view, parent, false);

        return new PostViewHolder(view, createOnClickListener(), activity, true);
    }

    private PostViewHolder.OnClickListener createOnClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callBack != null && callBack.enableClick()) {
                    selectedPostPosition = position;
                    callBack.onItemClick(getItemByPosition(position), view);
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                if (callBack != null && callBack.enableClick()) {
                    Post post = getItemByPosition(position);
                    likeController.handleLikeClickAction(activity, post);
                }
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callBack != null && callBack.enableClick()) {
                    callBack.onAuthorClick(getItemByPosition(position).getAuthorId(), view);
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((PostViewHolder) holder).bindData(postList.get(position));
    }

    public void setList(List<Post> list) {
        cleanSelectedPostInformation();
        postList.clear();
        postList.addAll(list);
        notifyDataSetChanged();
    }

    public void removeSelectedPost() {
        if (selectedPostPosition != RecyclerView.NO_POSITION) {
            postList.remove(selectedPostPosition);
            notifyItemRemoved(selectedPostPosition);
        }
    }

    public interface CallBack {
        void onItemClick(Post post, View view);
        void onAuthorClick(String authorId, View view);
        boolean enableClick();
    }
}
