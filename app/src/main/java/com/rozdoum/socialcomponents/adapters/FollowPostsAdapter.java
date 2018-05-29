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
import com.rozdoum.socialcomponents.adapters.holders.FollowPostViewHolder;
import com.rozdoum.socialcomponents.adapters.holders.PostViewHolder;
import com.rozdoum.socialcomponents.controllers.LikeController;
import com.rozdoum.socialcomponents.main.base.BaseActivity;
import com.rozdoum.socialcomponents.model.FollowingPost;

import java.util.ArrayList;
import java.util.List;


public class FollowPostsAdapter extends RecyclerView.Adapter<FollowPostViewHolder> {
    public static final String TAG = FollowPostsAdapter.class.getSimpleName();

    private List<FollowingPost> itemsList = new ArrayList<>();

    private CallBack callBack;

    private BaseActivity activity;

    private int selectedPostPosition = RecyclerView.NO_POSITION;

    public FollowPostsAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    @NonNull
    @Override
    public FollowPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.post_item_list_view, parent, false);
        return new FollowPostViewHolder(view, createOnClickListener(), activity, true);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowPostViewHolder holder, int position) {
        holder.bindData(itemsList.get(position));
    }

    public void setList(List<FollowingPost> list) {
        itemsList.clear();
        itemsList.addAll(list);
        notifyDataSetChanged();
    }

    private PostViewHolder.OnClickListener createOnClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callBack != null) {
                    callBack.onItemClick(getItemByPosition(position), view);
                    selectedPostPosition = position;
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                FollowingPost followingPost = getItemByPosition(position);
                likeController.handleLikeClickAction(activity, followingPost.getPostId());
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callBack != null) {
                    callBack.onAuthorClick(position, view);
                }
            }
        };
    }

    public FollowingPost getItemByPosition(int position) {
        return itemsList.get(position);
    }

    public void updateSelectedItem() {
        if (selectedPostPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPostPosition);
        }
    }

    public interface CallBack {
        void onItemClick(FollowingPost followingPost, View view);

        void onAuthorClick(int position, View view);
    }
}
