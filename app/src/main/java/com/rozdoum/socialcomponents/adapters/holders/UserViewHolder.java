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

package com.rozdoum.socialcomponents.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.UsersAdapter;
import com.rozdoum.socialcomponents.enums.FollowState;
import com.rozdoum.socialcomponents.managers.FollowManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.GlideApp;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.views.FollowButton;

/**
 * Created by Alexey on 03.05.18.
 */

public class UserViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = UserViewHolder.class.getSimpleName();

    private Context context;
    private ImageView photoImageView;
    private TextView nameTextView;
    private FollowButton followButton;

    private ProfileManager profileManager;

    public UserViewHolder(View view, final UsersAdapter.Callback onClickListener) {
        super(view);
        this.context = view.getContext();
        profileManager = ProfileManager.getInstance(context);

        nameTextView = view.findViewById(R.id.nameTextView);
        photoImageView = view.findViewById(R.id.photoImageView);
        followButton = view.findViewById(R.id.followButton);

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onItemClick(getAdapterPosition(), v);
            }
        });

        followButton.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onFollowButtonClick(getAdapterPosition(), followButton);
            }
        });
    }

    public void bindData(String profileId) {
        profileManager.getProfileSingleValue(profileId, createProfileChangeListener());
    }


    private OnObjectChangedListener<Profile> createProfileChangeListener() {
        return profile -> {

            nameTextView.setText(profile.getUsername());

            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId != null) {
                if (!currentUserId.equals(profile.getId())) {
                    FollowManager.getInstance(context).checkFollowState(currentUserId, profile.getId(), followState -> {
                        followButton.setVisibility(View.VISIBLE);
                        followButton.setState(followState);
                    });
                } else {
                    followButton.setState(FollowState.MY_PROFILE);
                }
            } else {
                followButton.setState(FollowState.NO_ONE_FOLLOW);
            }

            if (profile.getPhotoUrl() != null) {
                ImageUtil.loadImage(GlideApp.with(context), profile.getPhotoUrl(), photoImageView);
            }
        };
    }

}