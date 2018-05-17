/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.rozdoum.socialcomponents.adapters;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.holders.UserViewHolder;
import com.rozdoum.socialcomponents.views.FollowButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexey on 03.05.18.
 */

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = UsersAdapter.class.getSimpleName();

    private List<String> itemsList = new ArrayList<>();

    private Callback callback;

    public UsersAdapter() {
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return itemsList.size();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new UserViewHolder(inflater.inflate(R.layout.user_item_list_view, parent, false),
                callback);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((UserViewHolder) holder).bindData(itemsList.get(position));
    }

    public void setList(List<String> list) {
        notifyItemRangeRemoved(0, getItemCount());
        itemsList.clear();

        itemsList.addAll(list);
        notifyItemRangeInserted(0, getItemCount());
    }

    public void updateItem(int position) {
        notifyItemChanged(position);
    }

    public String getItemByPosition(int position) {
        return itemsList.get(position);
    }

    public interface Callback {
        void onItemClick(int position, View view);

        void onFollowButtonClick(int position, FollowButton followButton);
    }
}
