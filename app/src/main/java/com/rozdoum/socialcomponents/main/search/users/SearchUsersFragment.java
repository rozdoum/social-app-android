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

package com.rozdoum.socialcomponents.main.search.users;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.SearchUsersAdapter;
import com.rozdoum.socialcomponents.adapters.holders.UserViewHolder;
import com.rozdoum.socialcomponents.main.base.BaseFragment;
import com.rozdoum.socialcomponents.main.login.LoginActivity;
import com.rozdoum.socialcomponents.main.profile.ProfileActivity;
import com.rozdoum.socialcomponents.main.search.Searchable;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.AnimationUtils;
import com.rozdoum.socialcomponents.views.FollowButton;

import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.rozdoum.socialcomponents.main.usersList.UsersListActivity.UPDATE_FOLLOWING_STATE_REQ;
import static com.rozdoum.socialcomponents.main.usersList.UsersListActivity.UPDATE_FOLLOWING_STATE_RESULT_OK;

public class SearchUsersFragment extends BaseFragment<SearchUsersView, SearchUsersPresenter>
        implements SearchUsersView, Searchable {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SearchUsersAdapter usersAdapter;
    private TextView emptyListMessageTextView;
    private String lastSearchText = "";

    private boolean searchInProgress = false;

    private int selectedItemPosition = RecyclerView.NO_POSITION;

    public SearchUsersFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public SearchUsersPresenter createPresenter() {
        if (presenter == null) {
            return new SearchUsersPresenter(getActivity());
        }
        return presenter;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyListMessageTextView = view.findViewById(R.id.emptyListMessageTextView);
        emptyListMessageTextView.setText(getResources().getString(R.string.empty_user_search_message));

        initRecyclerView();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_FOLLOWING_STATE_REQ && resultCode == UPDATE_FOLLOWING_STATE_RESULT_OK) {
            updateSelectedItem();
        }

        if (requestCode == LoginActivity.LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            presenter.search(lastSearchText);
        }
    }

    @Override
    public void updateSelectedItem() {
        if (selectedItemPosition != RecyclerView.NO_POSITION) {
            usersAdapter.updateItem(selectedItemPosition);
        }
    }

    private void initRecyclerView() {
        usersAdapter = new SearchUsersAdapter(getActivity());
        usersAdapter.setCallback(new UserViewHolder.Callback() {
            @Override
            public void onItemClick(int position, View view) {
                if (!searchInProgress) {
                    selectedItemPosition = position;
                    Profile profile = usersAdapter.getItemByPosition(position);
                    openProfileActivity(profile.getId(), view);
                }
            }

            @Override
            public void onFollowButtonClick(int position, FollowButton followButton) {
                if (!searchInProgress) {
                    selectedItemPosition = position;
                    Profile profile = usersAdapter.getItemByPosition(position);
                    presenter.onFollowButtonClick(followButton.getState(), profile.getId());
                }
            }
        });

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(usersAdapter);

        presenter.loadUsersWithEmptySearch();
    }

    @SuppressLint("RestrictedApi")
    private void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ImageView imageView = view.findViewById(R.id.photoImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(getActivity(),
                            new android.util.Pair<>(imageView, getString(R.string.post_author_image_transition_name)));
            startActivityForResult(intent, UPDATE_FOLLOWING_STATE_REQ, options.toBundle());
        } else {
            startActivityForResult(intent, UPDATE_FOLLOWING_STATE_REQ);
        }
    }

    @Override
    public void search(String searchText) {
        lastSearchText = searchText;
        presenter.search(searchText);
    }

    @Override
    public void onSearchResultsReady(List<Profile> profiles) {
        hideLocalProgress();
        emptyListMessageTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        usersAdapter.setList(profiles);
    }

    @Override
    public void showLocalProgress() {
        searchInProgress = true;
        AnimationUtils.showViewByScaleWithoutDelay(progressBar);
    }

    @Override
    public void hideLocalProgress() {
        searchInProgress = false;
        AnimationUtils.hideViewByScale(progressBar);
    }
    @Override
    public void showEmptyListLayout() {
        hideLocalProgress();
        recyclerView.setVisibility(View.GONE);
        emptyListMessageTextView.setVisibility(View.VISIBLE);
    }
}
