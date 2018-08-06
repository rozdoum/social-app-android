/*
 *
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
 *
 */

package com.rozdoum.socialcomponents.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.GlideApp;
import com.rozdoum.socialcomponents.utils.ImageUtil;

/**
 * Created by Alexey on 11.05.18.
 */

public class UnfollowConfirmationDialog extends DialogFragment {
    public static final String TAG = UnfollowConfirmationDialog.class.getSimpleName();
    public static final String PROFILE = "EditCommentDialog.PROFILE";

    private Callback callback;
    private Profile profile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getActivity() instanceof Callback) {
            callback = (Callback) getActivity();
        } else {
            throw new RuntimeException(getActivity().getTitle() + " should implements Callback");
        }

        profile = (Profile) getArguments().get(PROFILE);

        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.dialog_confirmation_unfollow, null);

        ImageView imageView = view.findViewById(R.id.imageView);
        TextView confirmationMessageTextView = view.findViewById(R.id.confirmationMessageTextView);

        confirmationMessageTextView.setText(getString(R.string.unfollow_user_message, profile.getUsername()));

        ImageUtil.loadImage(GlideApp.with(this), profile.getPhotoUrl(), imageView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_title_unfollow, (dialog, which) -> {
                    callback.onUnfollowButtonClicked();
                    dialog.cancel();
                });

        return builder.create();
    }

    public interface Callback {
        void onUnfollowButtonClicked();
    }
}
