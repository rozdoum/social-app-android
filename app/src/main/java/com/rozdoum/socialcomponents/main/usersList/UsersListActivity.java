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

package com.rozdoum.socialcomponents.main.usersList;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.UsersAdapter;
import com.rozdoum.socialcomponents.adapters.holders.UserViewHolder;
import com.rozdoum.socialcomponents.main.base.BaseActivity;
import com.rozdoum.socialcomponents.main.login.LoginActivity;
import com.rozdoum.socialcomponents.main.profile.ProfileActivity;
import com.rozdoum.socialcomponents.views.FollowButton;

import java.util.List;

/**
 * Created by Alexey on 03.05.18.
 */

public class UsersListActivity extends BaseActivity<UsersListView, UsersListPresenter> implements UsersListView {
    private static final String TAG = UsersListActivity.class.getSimpleName();

    public static final String USER_ID_EXTRA_KEY = "UsersListActivity.USER_ID_EXTRA_KEY";
    public static final String USER_LIST_TYPE = "UsersListActivity.USER_LIST_TYPE";

    public static final int UPDATE_FOLLOWING_STATE_REQ = 1501;
    public static final int UPDATE_FOLLOWING_STATE_RESULT_OK = 1502;

    private UsersAdapter usersAdapter;
    private RecyclerView recyclerView;
    private String userID;

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private int userListType;
    private TextView emptyListMessageTextView;

    private int selectedItemPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        userID = getIntent().getStringExtra(USER_ID_EXTRA_KEY);
        userListType = getIntent().getIntExtra(USER_LIST_TYPE, -1);

        if (userListType == -1) {
            throw new IllegalArgumentException("USER_LIST_TYPE should be defined for " + this.getClass().getSimpleName());
        }

        initContentView();

        presenter.chooseActivityTitle(userListType);
        presenter.loadUsersList(userID, userListType, false);
    }

    @NonNull
    @Override
    public UsersListPresenter createPresenter() {
        if (presenter == null) {
            return new UsersListPresenter(this);
        }
        return presenter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_FOLLOWING_STATE_REQ && resultCode == UPDATE_FOLLOWING_STATE_RESULT_OK) {
            updateSelectedItem();
        }

        if (requestCode == LoginActivity.LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            presenter.loadUsersList(userID, userListType, true);
        }
    }

    private void initContentView() {
        if (recyclerView == null) {
            progressBar = findViewById(R.id.progressBar);
            swipeContainer = findViewById(R.id.swipeContainer);
            emptyListMessageTextView = findViewById(R.id.emptyListMessageTextView);

            swipeContainer.setOnRefreshListener(() -> presenter.onRefresh(userID, userListType));

            initProfilesListRecyclerView();
        }
    }

    private void initProfilesListRecyclerView() {
        recyclerView = findViewById(R.id.usersRecyclerView);
        usersAdapter = new UsersAdapter(this);
        usersAdapter.setCallback(new UserViewHolder.Callback() {
            @Override
            public void onItemClick(int position, View view) {
                selectedItemPosition = position;
                String userId = usersAdapter.getItemByPosition(position);
                openProfileActivity(userId, view);
            }

            @Override
            public void onFollowButtonClick(int position, FollowButton followButton) {
                selectedItemPosition = position;
                String userId = usersAdapter.getItemByPosition(position);
                presenter.onFollowButtonClick(followButton.getState(), userId);
            }
        });

        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));

        recyclerView.setAdapter(usersAdapter);

    }

    @SuppressLint("RestrictedApi")
    private void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(UsersListActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ImageView imageView = view.findViewById(R.id.photoImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(UsersListActivity.this,
                            new android.util.Pair<>(imageView, getString(R.string.post_author_image_transition_name)));
            startActivityForResult(intent, UPDATE_FOLLOWING_STATE_REQ, options.toBundle());
        } else {
            startActivityForResult(intent, UPDATE_FOLLOWING_STATE_REQ);
        }
    }

    @Override
    public void onProfilesIdsListLoaded(List<String> list) {
        usersAdapter.setList(list);
    }

    @Override
    public void showLocalProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLocalProgress() {
        progressBar.setVisibility(View.GONE);
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void showEmptyListMessage(String message) {
        emptyListMessageTextView.setVisibility(View.VISIBLE);
        emptyListMessageTextView.setText(message);
    }

    @Override
    public void hideEmptyListMessage() {
        emptyListMessageTextView.setVisibility(View.GONE);
    }

    @Override
    public void updateSelectedItem() {
        if (selectedItemPosition != RecyclerView.NO_POSITION) {
            usersAdapter.updateItem(selectedItemPosition);
        }
    }
}
